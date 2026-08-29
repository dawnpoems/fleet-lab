package kr.dawnpoem.fleetlab.navigation

@ConsistentCopyVisibility
data class Path private constructor(
    val start: NodeId,
    val edges: List<Edge>,
) {
    val destination: NodeId = start
    val nodeIds: List<NodeId> = listOf(start)
    val totalDistanceMeters: Double = 0.0

    companion object {
        fun create(
            start: NodeId,
            edges: List<Edge>,
        ): Path = Path(start, edges)
    }
}
