package kr.dawnpoem.fleetlab.navigation

interface PathPlanner {
    fun findPath(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): PathPlanningResult
}

sealed interface PathPlanningResult {
    data class Found(
        val path: Path,
    ) : PathPlanningResult

    data object Unreachable : PathPlanningResult
}
