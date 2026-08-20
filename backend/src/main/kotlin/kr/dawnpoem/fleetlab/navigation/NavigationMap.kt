package kr.dawnpoem.fleetlab.navigation

class NavigationMap private constructor(
    val id: NavigationMapId,
    val name: NavigationMapName,
    private val sourceNodes: Collection<Node>,
    private val sourceEdges: Collection<Edge>,
) {
    val nodes: List<Node>
        get() = sourceNodes.toList()

    val edges: List<Edge>
        get() = sourceEdges.toList()

    fun findNode(id: NodeId): Node? = null

    fun outgoingEdgesFrom(id: NodeId): List<Edge> = emptyList()

    companion object {
        fun create(
            id: NavigationMapId,
            name: NavigationMapName,
            nodes: Collection<Node>,
            edges: Collection<Edge>,
        ): NavigationMap = NavigationMap(id, name, nodes, edges)
    }
}
