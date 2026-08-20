package kr.dawnpoem.fleetlab.navigation

@JvmInline
value class NavigationMapName private constructor(
    val value: String,
) {
    companion object {
        fun of(rawValue: String): NavigationMapName = NavigationMapName(rawValue)
    }
}
