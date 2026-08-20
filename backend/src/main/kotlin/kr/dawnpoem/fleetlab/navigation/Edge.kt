package kr.dawnpoem.fleetlab.navigation

data class Edge(
    val id: EdgeId,
    val from: NodeId,
    val to: NodeId,
    val distanceMeters: Double,
) {
    init {
        require(from != to) { "Edge must connect different nodes" }
        require(distanceMeters.isFinite() && distanceMeters > 0.0) {
            "Edge distanceMeters must be positive and finite"
        }
    }
}
