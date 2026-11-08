package kr.dawnpoem.fleetlab.navigation.adapter.web

import kr.dawnpoem.fleetlab.navigation.NavigationMap
import java.util.UUID

data class MapResponse(
    val id: UUID,
    val name: String,
    val nodes: List<NodeResponse>,
    val edges: List<EdgeResponse>,
)

data class NodeResponse(
    val id: UUID,
    val xMeters: Double,
    val yMeters: Double,
)

data class EdgeResponse(
    val id: UUID,
    val from: UUID,
    val to: UUID,
    val distanceMeters: Double,
)

internal fun NavigationMap.toResponse(): MapResponse =
    MapResponse(
        id.value,
        name.value,
        nodes.map { NodeResponse(it.id.value, it.coordinate.xMeters, it.coordinate.yMeters) },
        edges.map { EdgeResponse(it.id.value, it.from.value, it.to.value, it.distanceMeters) },
    )
