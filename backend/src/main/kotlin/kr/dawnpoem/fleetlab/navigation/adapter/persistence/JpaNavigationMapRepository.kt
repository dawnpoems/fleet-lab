package kr.dawnpoem.fleetlab.navigation.adapter.persistence

import jakarta.persistence.EntityManager
import kr.dawnpoem.fleetlab.navigation.Coordinate
import kr.dawnpoem.fleetlab.navigation.Edge
import kr.dawnpoem.fleetlab.navigation.EdgeId
import kr.dawnpoem.fleetlab.navigation.NavigationMap
import kr.dawnpoem.fleetlab.navigation.NavigationMapId
import kr.dawnpoem.fleetlab.navigation.NavigationMapName
import kr.dawnpoem.fleetlab.navigation.Node
import kr.dawnpoem.fleetlab.navigation.NodeId
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JpaNavigationMapRepository(
    private val entityManager: EntityManager,
) : NavigationMapRepository {
    @Transactional
    override fun save(map: NavigationMap) {
        entityManager.persist(NavigationMapEntity(map.id.value, map.name.value))
        entityManager.flush()

        map.nodes.forEach { node ->
            entityManager.persist(
                NavigationNodeEntity(node.id.value, map.id.value, node.coordinate.xMeters, node.coordinate.yMeters),
            )
        }
        entityManager.flush()

        map.edges.forEach { edge ->
            entityManager.persist(
                NavigationEdgeEntity(edge.id.value, map.id.value, edge.from.value, edge.to.value, edge.distanceMeters),
            )
        }
        entityManager.flush()
    }

    @Transactional(readOnly = true)
    override fun findById(id: NavigationMapId): NavigationMap? {
        val map = entityManager.find(NavigationMapEntity::class.java, id.value) ?: return null
        val nodes =
            entityManager
                .createQuery("select n from NavigationNodeEntity n where n.mapId = :mapId", NavigationNodeEntity::class.java)
                .setParameter("mapId", id.value)
                .resultList
                .map { Node(NodeId(it.id), Coordinate(it.xMeters, it.yMeters)) }
        val edges =
            entityManager
                .createQuery("select e from NavigationEdgeEntity e where e.mapId = :mapId", NavigationEdgeEntity::class.java)
                .setParameter("mapId", id.value)
                .resultList
                .map { Edge(EdgeId(it.id), NodeId(it.fromNodeId), NodeId(it.toNodeId), it.distanceMeters) }

        return NavigationMap.create(id, NavigationMapName.of(map.name), nodes, edges)
    }
}
