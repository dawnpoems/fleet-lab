package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DijkstraPathPlannerTest {
    private val planner: PathPlanner = DijkstraPathPlanner()
    private val nodeA = NodeId(uuid(101))
    private val nodeB = NodeId(uuid(102))
    private val nodeC = NodeId(uuid(103))
    private val nodeD = NodeId(uuid(104))
    private val unknownNode = NodeId(uuid(999))

    @Test
    fun `finds a direct path`() {
        val edgeAB = edge(201, nodeA, nodeB, 1.0)
        val map = navigationMap(edgeAB)

        val path = foundPath(planner.findPath(map, nodeA, nodeB))

        assertEquals(listOf(edgeAB), path.edges)
    }

    @Test
    fun `finds a shorter multi-edge path instead of a longer direct path`() {
        val edgeAB = edge(201, nodeA, nodeB, 1.0)
        val edgeBD = edge(202, nodeB, nodeD, 1.0)
        val edgeAD = edge(203, nodeA, nodeD, 3.0)
        val map = navigationMap(edgeAD, edgeBD, edgeAB)

        val path = foundPath(planner.findPath(map, nodeA, nodeD))

        assertEquals(listOf(edgeAB, edgeBD), path.edges)
    }

    @Test
    fun `respects edge direction`() {
        val edgeAB = edge(201, nodeA, nodeB, 1.0)
        val map = navigationMap(edgeAB)

        val result = planner.findPath(map, nodeB, nodeA)

        assertEquals(PathPlanningResult.Unreachable, result)
    }

    @Test
    fun `returns unreachable for disconnected graph regions`() {
        val edgeAB = edge(201, nodeA, nodeB, 1.0)
        val edgeCD = edge(202, nodeC, nodeD, 1.0)
        val map = navigationMap(edgeAB, edgeCD)

        val result = planner.findPath(map, nodeA, nodeD)

        assertEquals(PathPlanningResult.Unreachable, result)
    }

    @Test
    fun `returns an empty path when start equals destination`() {
        val map = navigationMap(edge(201, nodeA, nodeB, 1.0))

        val path = foundPath(planner.findPath(map, nodeA, nodeA))

        assertEquals(Path.create(nodeA, emptyList()), path)
    }

    @Test
    fun `rejects a start node that is not in the map`() {
        val map = navigationMap(edge(201, nodeA, nodeB, 1.0))

        assertFailsWith<IllegalArgumentException> {
            planner.findPath(map, unknownNode, nodeB)
        }
    }

    @Test
    fun `rejects a destination node that is not in the map`() {
        val map = navigationMap(edge(201, nodeA, nodeB, 1.0))

        assertFailsWith<IllegalArgumentException> {
            planner.findPath(map, nodeA, unknownNode)
        }
    }

    @Test
    fun `chooses the shorter parallel edge`() {
        val longerEdge = edge(201, nodeA, nodeB, 2.0)
        val shorterEdge = edge(202, nodeA, nodeB, 1.0)
        val map = navigationMap(longerEdge, shorterEdge)

        val path = foundPath(planner.findPath(map, nodeA, nodeB))

        assertEquals(listOf(shorterEdge), path.edges)
    }

    @Test
    fun `handles cycles while finding the shortest path`() {
        val edgeAB = edge(201, nodeA, nodeB, 1.0)
        val edgeBA = edge(202, nodeB, nodeA, 1.0)
        val edgeBC = edge(203, nodeB, nodeC, 1.0)
        val map = navigationMap(edgeBA, edgeBC, edgeAB)

        val path = foundPath(planner.findPath(map, nodeA, nodeC))

        assertEquals(listOf(edgeAB, edgeBC), path.edges)
    }

    @Test
    fun `breaks equal-distance ties by the full edge identifier path`() {
        val edgeAB = edge(301, nodeA, nodeB, 1.0)
        val edgeBD = edge(304, nodeB, nodeD, 1.0)
        val edgeAC = edge(302, nodeA, nodeC, 1.0)
        val edgeCD = edge(303, nodeC, nodeD, 1.0)
        val map = navigationMap(edgeAC, edgeCD, edgeAB, edgeBD)

        val path = foundPath(planner.findPath(map, nodeA, nodeD))

        assertEquals(listOf(edgeAB, edgeBD), path.edges)
    }

    @Test
    fun `does not use edge count as an equal-distance tie breaker`() {
        val edgeAB = edge(100, nodeA, nodeB, 1.0)
        val edgeBD = edge(200, nodeB, nodeD, 1.0)
        val edgeAD = edge(300, nodeA, nodeD, 2.0)
        val map = navigationMap(edgeAD, edgeBD, edgeAB)

        val path = foundPath(planner.findPath(map, nodeA, nodeD))

        assertEquals(listOf(edgeAB, edgeBD), path.edges)
    }

    @Test
    fun `compares exact double distances before edge identifiers`() {
        val edgeAB = edge(100, nodeA, nodeB, 0.1)
        val edgeBD = edge(200, nodeB, nodeD, 0.2)
        val edgeAD = edge(300, nodeA, nodeD, 0.3)
        val map = navigationMap(edgeAB, edgeBD, edgeAD)

        val path = foundPath(planner.findPath(map, nodeA, nodeD))

        assertEquals(listOf(edgeAD), path.edges)
    }

    @Test
    fun `chooses a finite path when another path distance overflows`() {
        val overflowingEdgeAB = edge(100, nodeA, nodeB, Double.MAX_VALUE)
        val overflowingEdgeBD = edge(200, nodeB, nodeD, Double.MAX_VALUE)
        val finiteEdgeAC = edge(300, nodeA, nodeC, 1.0)
        val finiteEdgeCD = edge(400, nodeC, nodeD, 1.0)
        val map = navigationMap(overflowingEdgeAB, overflowingEdgeBD, finiteEdgeAC, finiteEdgeCD)

        val path = foundPath(planner.findPath(map, nodeA, nodeD))

        assertEquals(listOf(finiteEdgeAC, finiteEdgeCD), path.edges)
    }

    @Test
    fun `throws when every path to a reachable destination overflows`() {
        val edgeAB = edge(100, nodeA, nodeB, Double.MAX_VALUE)
        val edgeBC = edge(200, nodeB, nodeC, Double.MAX_VALUE)
        val map = navigationMap(edgeAB, edgeBC)

        assertFailsWith<ArithmeticException> {
            planner.findPath(map, nodeA, nodeC)
        }
    }

    @Test
    fun `returns the same equal-distance path regardless of map edge order`() {
        val edgeAB = edge(301, nodeA, nodeB, 1.0)
        val edgeBD = edge(304, nodeB, nodeD, 1.0)
        val edgeAC = edge(302, nodeA, nodeC, 1.0)
        val edgeCD = edge(303, nodeC, nodeD, 1.0)
        val firstMap = navigationMap(edgeAB, edgeBD, edgeAC, edgeCD)
        val secondMap = navigationMap(edgeCD, edgeAC, edgeBD, edgeAB)

        val firstPath = foundPath(planner.findPath(firstMap, nodeA, nodeD))
        val secondPath = foundPath(planner.findPath(secondMap, nodeA, nodeD))

        assertEquals(listOf(edgeAB, edgeBD), firstPath.edges)
        assertEquals(firstPath, secondPath)
    }

    private fun foundPath(result: PathPlanningResult): Path = assertIs<PathPlanningResult.Found>(result).path

    private fun navigationMap(vararg edges: Edge): NavigationMap {
        val nodeIds = edges.flatMap { edge -> listOf(edge.from, edge.to) }.distinct()
        val nodes =
            nodeIds.mapIndexed { index, nodeId ->
                Node(
                    id = nodeId,
                    coordinate = Coordinate(index.toDouble(), 0.0),
                )
            }

        return NavigationMap.create(
            id = NavigationMapId(uuid(1)),
            name = NavigationMapName.of("Test Map"),
            nodes = nodes,
            edges = edges.asList(),
        )
    }

    private fun edge(
        identifier: Int,
        from: NodeId,
        to: NodeId,
        distanceMeters: Double,
    ): Edge =
        Edge(
            id = EdgeId(uuid(identifier)),
            from = from,
            to = to,
            distanceMeters = distanceMeters,
        )

    private fun uuid(identifier: Int): UUID {
        val suffix = identifier.toString().padStart(12, '0')

        return UUID.fromString("00000000-0000-0000-0000-$suffix")
    }
}
