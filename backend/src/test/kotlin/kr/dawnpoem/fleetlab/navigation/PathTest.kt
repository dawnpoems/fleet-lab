package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class PathTest {
    private val nodeA = NodeId(uuid(101))
    private val nodeB = NodeId(uuid(102))
    private val nodeC = NodeId(uuid(103))
    private val edgeAB = edge(201, nodeA, nodeB, 1.25)
    private val edgeBC = edge(202, nodeB, nodeC, 2.75)
    private val edgeCA = edge(203, nodeC, nodeA, 3.0)

    @Test
    fun `creates an empty path at the start node`() {
        val path = Path.create(start = nodeA, edges = emptyList())

        assertEquals(nodeA, path.start)
        assertEquals(emptyList(), path.edges)
        assertEquals(nodeA, path.destination)
        assertEquals(listOf(nodeA), path.nodeIds)
        assertEquals(0.0, path.totalDistanceMeters)
    }

    @Test
    fun `derives the destination from the last edge`() {
        val path = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))

        assertEquals(nodeC, path.destination)
    }

    @Test
    fun `derives ordered node identifiers from edges`() {
        val path = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))

        assertEquals(listOf(nodeA, nodeB, nodeC), path.nodeIds)
    }

    @Test
    fun `sums edge distances`() {
        val path = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))

        assertEquals(4.0, path.totalDistanceMeters)
    }

    @Test
    fun `preserves the ordered edge list`() {
        val path = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))

        assertEquals(listOf(edgeAB, edgeBC), path.edges)
    }

    @Test
    fun `copies the input edge list`() {
        val inputEdges = mutableListOf(edgeAB)
        val path = Path.create(start = nodeA, edges = inputEdges)

        inputEdges.clear()

        assertEquals(listOf(edgeAB), path.edges)
    }

    @Test
    fun `rejects a first edge that does not start at the path start`() {
        assertFailsWith<IllegalArgumentException> {
            Path.create(start = nodeA, edges = listOf(edgeBC))
        }
    }

    @Test
    fun `rejects disconnected consecutive edges`() {
        assertFailsWith<IllegalArgumentException> {
            Path.create(start = nodeA, edges = listOf(edgeAB, edgeCA))
        }
    }

    @Test
    fun `rejects a non-finite total distance`() {
        val longEdgeAB = edge(204, nodeA, nodeB, Double.MAX_VALUE)
        val longEdgeBC = edge(205, nodeB, nodeC, Double.MAX_VALUE)

        assertFailsWith<IllegalArgumentException> {
            Path.create(start = nodeA, edges = listOf(longEdgeAB, longEdgeBC))
        }
    }

    @Test
    fun `has value equality based on start and edges`() {
        val first = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))
        val second = Path.create(start = nodeA, edges = listOf(edgeAB, edgeBC))

        assertEquals(first, second)
    }

    @Test
    fun `distinguishes paths that choose different parallel edges`() {
        val parallelEdgeAB = edge(204, nodeA, nodeB, 1.25)

        val first = Path.create(start = nodeA, edges = listOf(edgeAB))
        val second = Path.create(start = nodeA, edges = listOf(parallelEdgeAB))

        assertNotEquals(first, second)
    }

    @Test
    fun `allows a continuous cycle`() {
        val edges = listOf(edgeAB, edgeBC, edgeCA)

        val path = Path.create(start = nodeA, edges = edges)

        assertEquals(edges, path.edges)
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
