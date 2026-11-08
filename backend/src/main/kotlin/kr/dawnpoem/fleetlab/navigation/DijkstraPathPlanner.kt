package kr.dawnpoem.fleetlab.navigation

import java.util.PriorityQueue

class DijkstraPathPlanner : PathPlanner {
    override fun findPath(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): PathPlanningResult {
        require(map.findNode(start) != null) { "Start node $start does not exist in navigation map ${map.id}" }
        require(map.findNode(destination) != null) {
            "Destination node $destination does not exist in navigation map ${map.id}"
        }

        if (start == destination) {
            return PathPlanningResult.Found(Path.create(start, emptyList()))
        }

        val path = findShortestFinitePath(map, start, destination)
        if (path != null) {
            return PathPlanningResult.Found(path)
        }

        if (isReachableIgnoringDistance(map, start, destination)) {
            throw ArithmeticException("Every path from $start to $destination exceeds the finite Double distance range")
        }

        return PathPlanningResult.Unreachable
    }

    private fun findShortestFinitePath(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): Path? {
        val initialCandidate = PathCandidate(node = start, distanceMeters = 0.0, edges = emptyList())
        val bestCandidateByNode = mutableMapOf(start to initialCandidate)
        val candidates = PriorityQueue(PATH_CANDIDATE_COMPARATOR)
        candidates.add(initialCandidate)

        while (candidates.isNotEmpty()) {
            val current = candidates.remove()
            if (bestCandidateByNode[current.node] != current) {
                continue
            }
            if (current.node == destination) {
                return Path.create(start, current.edges)
            }

            map
                .outgoingEdgesFrom(current.node)
                .sortedWith(EDGE_COMPARATOR)
                .forEach { edge ->
                    val distanceMeters = current.distanceMeters + edge.distanceMeters
                    if (!distanceMeters.isFinite()) {
                        return@forEach
                    }

                    val candidate =
                        PathCandidate(
                            node = edge.to,
                            distanceMeters = distanceMeters,
                            edges = current.edges + edge,
                        )
                    val bestCandidate = bestCandidateByNode[edge.to]
                    if (bestCandidate == null || PATH_CANDIDATE_COMPARATOR.compare(candidate, bestCandidate) < 0) {
                        bestCandidateByNode[edge.to] = candidate
                        candidates.add(candidate)
                    }
                }
        }

        return null
    }

    private fun isReachableIgnoringDistance(
        map: NavigationMap,
        start: NodeId,
        destination: NodeId,
    ): Boolean {
        val visited = mutableSetOf(start)
        val nodesToVisit = ArrayDeque<NodeId>()
        nodesToVisit.addLast(start)

        while (nodesToVisit.isNotEmpty()) {
            val current = nodesToVisit.removeFirst()
            if (current == destination) {
                return true
            }

            map.outgoingEdgesFrom(current).forEach { edge ->
                if (visited.add(edge.to)) {
                    nodesToVisit.addLast(edge.to)
                }
            }
        }

        return false
    }

    private data class PathCandidate(
        val node: NodeId,
        val distanceMeters: Double,
        val edges: List<Edge>,
    )

    private companion object {
        val EDGE_COMPARATOR: Comparator<Edge> = compareBy { edge -> edge.id.value }

        val PATH_CANDIDATE_COMPARATOR =
            Comparator<PathCandidate> { left, right ->
                val distanceComparison = left.distanceMeters.compareTo(right.distanceMeters)
                if (distanceComparison != 0) {
                    distanceComparison
                } else {
                    compareEdgePaths(left.edges, right.edges)
                }
            }

        fun compareEdgePaths(
            left: List<Edge>,
            right: List<Edge>,
        ): Int {
            repeat(minOf(left.size, right.size)) { index ->
                val identifierComparison = left[index].id.value.compareTo(right[index].id.value)
                if (identifierComparison != 0) {
                    return identifierComparison
                }
            }

            return left.size.compareTo(right.size)
        }
    }
}
