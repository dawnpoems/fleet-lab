package kr.dawnpoem.fleetlab.navigation.adapter

import kr.dawnpoem.fleetlab.navigation.DijkstraPathPlanner
import kr.dawnpoem.fleetlab.navigation.PathPlanner
import kr.dawnpoem.fleetlab.navigation.application.NavigationMaps
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class NavigationConfiguration {
    @Bean
    fun navigationMaps(
        repository: NavigationMapRepository,
        planner: PathPlanner,
    ): NavigationMaps = NavigationMaps(repository, planner)

    @Bean
    fun pathPlanner(): PathPlanner = DijkstraPathPlanner()
}
