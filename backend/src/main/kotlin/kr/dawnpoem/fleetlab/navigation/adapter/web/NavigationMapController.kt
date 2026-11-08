package kr.dawnpoem.fleetlab.navigation.adapter.web

import kr.dawnpoem.fleetlab.navigation.NavigationMapId
import kr.dawnpoem.fleetlab.navigation.application.CreateMapCommand
import kr.dawnpoem.fleetlab.navigation.application.EdgeInput
import kr.dawnpoem.fleetlab.navigation.application.NavigationMaps
import kr.dawnpoem.fleetlab.navigation.application.NodeInput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/maps")
class NavigationMapController(
    private val maps: NavigationMaps,
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateMapRequest,
    ): ResponseEntity<MapResponse> {
        val map =
            maps.create(
                CreateMapCommand(
                    request.name,
                    request.nodes.map { NodeInput(it.key, it.xMeters, it.yMeters) },
                    request.edges.map { EdgeInput(it.from, it.to, it.distanceMeters) },
                ),
            )
        return ResponseEntity.created(URI.create("/api/maps/${map.id.value}")).body(map.toResponse())
    }

    @GetMapping("/{mapId}")
    fun get(
        @PathVariable mapId: UUID,
    ): MapResponse = maps.get(NavigationMapId(mapId)).toResponse()
}

data class CreateMapRequest(
    val name: String,
    val nodes: List<CreateNodeRequest>,
    val edges: List<CreateEdgeRequest>,
)

data class CreateNodeRequest(
    val key: String,
    val xMeters: Double,
    val yMeters: Double,
)

data class CreateEdgeRequest(
    val from: String,
    val to: String,
    val distanceMeters: Double,
)
