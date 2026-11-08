package kr.dawnpoem.fleetlab.navigation.adapter

import kr.dawnpoem.fleetlab.navigation.application.NavigationMaps
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class NavigationConfiguration {
    @Bean
    fun navigationMaps(repository: NavigationMapRepository): NavigationMaps = NavigationMaps(repository)
}
