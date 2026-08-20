package kr.dawnpoem.fleetlab.navigation

data class Edge(
    val id: EdgeId,
    val from: NodeId,
    val to: NodeId,
    val distanceMeters: Double,
)
