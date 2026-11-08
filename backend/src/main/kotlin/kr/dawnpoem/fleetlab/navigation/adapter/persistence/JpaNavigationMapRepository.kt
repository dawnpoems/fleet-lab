package kr.dawnpoem.fleetlab.navigation.adapter.persistence

import kr.dawnpoem.fleetlab.navigation.NavigationMap
import kr.dawnpoem.fleetlab.navigation.NavigationMapId
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import org.springframework.stereotype.Repository

@Repository
class JpaNavigationMapRepository : NavigationMapRepository {
    override fun save(map: NavigationMap) = Unit

    override fun findById(id: NavigationMapId): NavigationMap? = null
}
