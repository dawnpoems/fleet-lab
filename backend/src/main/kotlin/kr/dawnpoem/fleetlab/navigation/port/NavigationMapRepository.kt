package kr.dawnpoem.fleetlab.navigation.port

import kr.dawnpoem.fleetlab.navigation.NavigationMap
import kr.dawnpoem.fleetlab.navigation.NavigationMapId

interface NavigationMapRepository {
    fun save(map: NavigationMap)

    fun findById(id: NavigationMapId): NavigationMap?
}
