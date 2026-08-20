package kr.dawnpoem.fleetlab.navigation

@JvmInline
value class NavigationMapName private constructor(
    val value: String,
) {
    companion object {
        fun of(rawValue: String): NavigationMapName {
            val normalizedValue = rawValue.trim()

            require(normalizedValue.length in 1..100) {
                "Navigation map name must be between 1 and 100 characters"
            }

            return NavigationMapName(normalizedValue)
        }
    }
}
