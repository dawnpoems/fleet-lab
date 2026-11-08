package kr.dawnpoem.fleetlab.navigation.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "navigation_map")
class NavigationMapEntity(
    @Id
    val id: UUID,
    @Column(nullable = false, length = 100, updatable = false)
    val name: String,
)
