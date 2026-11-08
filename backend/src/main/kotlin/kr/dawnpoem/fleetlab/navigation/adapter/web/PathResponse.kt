package kr.dawnpoem.fleetlab.navigation.adapter.web

import kr.dawnpoem.fleetlab.navigation.PathPlanningResult
import java.util.UUID

sealed interface PathResponse {
    data class Found(
        val path: PathDetails,
    ) : PathResponse {
        val status: String = "Found"
    }

    data object Unreachable : PathResponse {
        val status: String = "Unreachable"
    }
}

data class PathDetails(
    val start: UUID,
    val destination: UUID,
    val nodeIds: List<UUID>,
    val edgeIds: List<UUID>,
    val totalDistanceMeters: Double,
)

internal fun PathPlanningResult.toResponse(): PathResponse =
    when (this) {
        is PathPlanningResult.Found ->
            PathResponse.Found(
                PathDetails(
                    path.start.value,
                    path.destination.value,
                    path.nodeIds.map { it.value },
                    path.edges.map { it.id.value },
                    path.totalDistanceMeters,
                ),
            )
        PathPlanningResult.Unreachable -> PathResponse.Unreachable
    }
