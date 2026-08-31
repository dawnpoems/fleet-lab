package kr.dawnpoem.fleetlab.navigation

@ConsistentCopyVisibility
data class Path private constructor(
    val start: NodeId,
    val edges: List<Edge>,
) {
    val destination: NodeId = edges.lastOrNull()?.to ?: start
    val nodeIds: List<NodeId> =
        buildList(edges.size + 1) {
            add(start)
            edges.forEach { edge -> add(edge.to) }
        }
    val totalDistanceMeters: Double = edges.sumOf(Edge::distanceMeters)

    init {
        var expectedFrom = start

        edges.forEach { edge ->
            require(edge.from == expectedFrom) {
                "Path edge ${edge.id} must start at $expectedFrom but starts at ${edge.from}"
            }
            expectedFrom = edge.to
        }

        require(totalDistanceMeters.isFinite()) {
            "Path total distance must be finite"
        }
    }

    companion object {
        fun create(
            start: NodeId,
            edges: List<Edge>,
        ): Path = Path(start, java.util.List.copyOf(edges))
    }
}
