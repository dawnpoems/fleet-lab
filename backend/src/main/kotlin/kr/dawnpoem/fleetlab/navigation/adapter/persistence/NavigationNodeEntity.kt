package kr.dawnpoem.fleetlab.navigation.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "navigation_node")
class NavigationNodeEntity(
    @Id
    val id: UUID,
    @Column(name = "map_id", nullable = false, updatable = false)
    val mapId: UUID,
    @Column(name = "x_meters", nullable = false, updatable = false)
    val xMeters: Double,
    @Column(name = "y_meters", nullable = false, updatable = false)
    val yMeters: Double,
)
