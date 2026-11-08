package kr.dawnpoem.fleetlab.navigation.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "navigation_edge")
class NavigationEdgeEntity(
    @Id
    val id: UUID,
    @Column(name = "map_id", nullable = false, updatable = false)
    val mapId: UUID,
    @Column(name = "from_node_id", nullable = false, updatable = false)
    val fromNodeId: UUID,
    @Column(name = "to_node_id", nullable = false, updatable = false)
    val toNodeId: UUID,
    @Column(name = "distance_meters", nullable = false, updatable = false)
    val distanceMeters: Double,
)
