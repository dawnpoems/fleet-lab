package kr.dawnpoem.fleetlab.navigation

class DijkstraPathPlanner : PathPlanner {
    override fun findPath(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): PathPlanningResult = PathPlanningResult.Unreachable
}
