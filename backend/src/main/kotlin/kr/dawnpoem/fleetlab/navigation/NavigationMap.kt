package kr.dawnpoem.fleetlab.navigation

class NavigationMap private constructor(
    val id: NavigationMapId,
    val name: NavigationMapName,
    val nodes: List<Node>,
    val edges: List<Edge>,
    private val nodesById: Map<NodeId, Node>,
    private val outgoingEdgesByNodeId: Map<NodeId, List<Edge>>,
) {
    fun findNode(id: NodeId): Node? = nodesById[id]

    fun outgoingEdgesFrom(id: NodeId): List<Edge> {
        require(nodesById.containsKey(id)) { "Node $id does not exist in navigation map ${this.id}" }

        return outgoingEdgesByNodeId[id].orEmpty()
    }

    companion object {
        fun create(
            id: NavigationMapId,
            name: NavigationMapName,
            nodes: Collection<Node>,
            edges: Collection<Edge>,
        ): NavigationMap {
            val nodeSnapshot = java.util.List.copyOf(nodes)
            val edgeSnapshot = java.util.List.copyOf(edges)

            require(nodeSnapshot.size >= 2) { "Navigation map must contain at least two nodes" }
            require(edgeSnapshot.isNotEmpty()) { "Navigation map must contain at least one edge" }

            val nodesById = nodeSnapshot.associateBy(Node::id)
            require(nodesById.size == nodeSnapshot.size) { "Navigation map contains duplicate node IDs" }
            require(edgeSnapshot.distinctBy(Edge::id).size == edgeSnapshot.size) {
                "Navigation map contains duplicate edge IDs"
            }

            edgeSnapshot.forEach { edge ->
                require(nodesById.containsKey(edge.from)) {
                    "Edge ${edge.id} references unknown start node ${edge.from}"
                }
                require(nodesById.containsKey(edge.to)) {
                    "Edge ${edge.id} references unknown destination node ${edge.to}"
                }
            }

            val connectedNodeIds =
                buildSet {
                    edgeSnapshot.forEach { edge ->
                        add(edge.from)
                        add(edge.to)
                    }
                }
            require(nodeSnapshot.all { node -> node.id in connectedNodeIds }) {
                "Navigation map contains an isolated node"
            }

            val outgoingEdgesByNodeId =
                edgeSnapshot
                    .groupBy(Edge::from)
                    .mapValues { (_, outgoingEdges) -> java.util.List.copyOf(outgoingEdges) }

            return NavigationMap(
                id = id,
                name = name,
                nodes = nodeSnapshot,
                edges = edgeSnapshot,
                nodesById = nodesById,
                outgoingEdgesByNodeId = outgoingEdgesByNodeId,
            )
        }
    }
}
