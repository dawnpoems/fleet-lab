package kr.dawnpoem.fleetlab.navigation.application

import kr.dawnpoem.fleetlab.navigation.Coordinate
import kr.dawnpoem.fleetlab.navigation.Edge
import kr.dawnpoem.fleetlab.navigation.EdgeId
import kr.dawnpoem.fleetlab.navigation.NavigationMap
import kr.dawnpoem.fleetlab.navigation.NavigationMapId
import kr.dawnpoem.fleetlab.navigation.NavigationMapName
import kr.dawnpoem.fleetlab.navigation.Node
import kr.dawnpoem.fleetlab.navigation.NodeId
import kr.dawnpoem.fleetlab.navigation.PathPlanner
import kr.dawnpoem.fleetlab.navigation.PathPlanningResult
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import java.util.UUID

class NavigationMaps(
    private val repository: NavigationMapRepository,
    private val planner: PathPlanner,
) {
    fun create(command: CreateMapCommand): NavigationMap {
        require(command.nodes.all { it.key.isNotBlank() }) { "Node keys must not be blank" }
        require(
            command.nodes
                .map { it.key }
                .distinct()
                .size == command.nodes.size,
        ) { "Node keys must be unique" }
        val nodesByKey =
            command.nodes.associate { input ->
                input.key to Node(NodeId(UUID.randomUUID()), Coordinate(input.xMeters, input.yMeters))
            }
        val edges =
            command.edges.map { input ->
                val from = requireNotNull(nodesByKey[input.from]) { "Unknown start node key" }
                val to = requireNotNull(nodesByKey[input.to]) { "Unknown destination node key" }
                Edge(EdgeId(UUID.randomUUID()), from.id, to.id, input.distanceMeters)
            }
        val map =
            NavigationMap.create(
                NavigationMapId(UUID.randomUUID()),
                NavigationMapName.of(command.name),
                nodesByKey.values,
                edges,
            )
        repository.save(map)
        return map
    }

    fun get(id: NavigationMapId): NavigationMap = repository.findById(id) ?: throw MapNotFoundException(id)

    fun findPath(
        id: NavigationMapId,
        start: NodeId,
        destination: NodeId,
    ): PathPlanningResult = planner.findPath(get(id), start, destination)
}

class MapNotFoundException(
    id: NavigationMapId,
) : RuntimeException("Navigation map ${id.value} does not exist")

data class CreateMapCommand(
    val name: String,
    val nodes: List<NodeInput>,
    val edges: List<EdgeInput>,
)

data class NodeInput(
    val key: String,
    val xMeters: Double,
    val yMeters: Double,
)

data class EdgeInput(
    val from: String,
    val to: String,
    val distanceMeters: Double,
)
