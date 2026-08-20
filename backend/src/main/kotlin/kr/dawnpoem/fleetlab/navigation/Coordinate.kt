package kr.dawnpoem.fleetlab.navigation

data class Coordinate(
    val xMeters: Double,
    val yMeters: Double,
) {
    init {
        require(xMeters.isFinite()) { "xMeters must be finite" }
        require(yMeters.isFinite()) { "yMeters must be finite" }
    }
}
