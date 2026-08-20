package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NavigationMapTest {
    private val mapId = NavigationMapId(uuid(1))
    private val mapName = NavigationMapName.of("Main Warehouse")
    private val nodeA = node(101, 0.0, 0.0)
    private val nodeB = node(102, 10.0, 0.0)
    private val nodeC = node(103, 20.0, 0.0)
    private val nodeD = node(104, 30.0, 0.0)
    private val edgeAB = edge(201, nodeA, nodeB)

    @Test
    fun `creates a map with its identity name and graph`() {
        val map = createMap()

        assertEquals(mapId, map.id)
        assertEquals(mapName, map.name)
        assertEquals(setOf(nodeA, nodeB), map.nodes.toSet())
        assertEquals(setOf(edgeAB), map.edges.toSet())
    }

    @Test
    fun `rejects an empty graph`() {
        assertFailsWith<IllegalArgumentException> {
            createMap(nodes = emptyList(), edges = emptyList())
        }
    }

    @Test
    fun `rejects a graph with only one node`() {
        assertFailsWith<IllegalArgumentException> {
            createMap(nodes = listOf(nodeA), edges = emptyList())
        }
    }

    @Test
    fun `rejects a graph without edges`() {
        assertFailsWith<IllegalArgumentException> {
            createMap(nodes = listOf(nodeA, nodeB), edges = emptyList())
        }
    }

    @Test
    fun `rejects duplicate node identifiers`() {
        val duplicateNode =
            Node(
                id = nodeA.id,
                coordinate = Coordinate(1.0, 1.0),
            )

        assertFailsWith<IllegalArgumentException> {
            createMap(
                nodes = listOf(nodeA, duplicateNode, nodeB),
                edges = listOf(edgeAB),
            )
        }
    }

    @Test
    fun `rejects duplicate edge identifiers`() {
        val duplicateEdgeId =
            Edge(
                id = edgeAB.id,
                from = nodeB.id,
                to = nodeA.id,
                distanceMeters = 2.0,
            )

        assertFailsWith<IllegalArgumentException> {
            createMap(edges = listOf(edgeAB, duplicateEdgeId))
        }
    }

    @Test
    fun `rejects an edge with an unknown start node`() {
        val unknownNode = node(999, 40.0, 0.0)
        val edgeFromUnknown = edge(202, unknownNode, nodeA)

        assertFailsWith<IllegalArgumentException> {
            createMap(edges = listOf(edgeAB, edgeFromUnknown))
        }
    }

    @Test
    fun `rejects an edge with an unknown destination node`() {
        val unknownNode = node(999, 40.0, 0.0)
        val edgeToUnknown = edge(202, nodeA, unknownNode)

        assertFailsWith<IllegalArgumentException> {
            createMap(edges = listOf(edgeAB, edgeToUnknown))
        }
    }

    @Test
    fun `rejects an isolated node`() {
        assertFailsWith<IllegalArgumentException> {
            createMap(
                nodes = listOf(nodeA, nodeB, nodeC),
                edges = listOf(edgeAB),
            )
        }
    }

    @Test
    fun `allows parallel edges with different identifiers`() {
        val parallelEdge = edge(202, nodeA, nodeB, distanceMeters = 2.0)

        val map = createMap(edges = listOf(edgeAB, parallelEdge))

        assertEquals(setOf(edgeAB, parallelEdge), map.edges.toSet())
    }

    @Test
    fun `allows disconnected graph regions`() {
        val edgeCD = edge(202, nodeC, nodeD)

        val map =
            createMap(
                nodes = listOf(nodeA, nodeB, nodeC, nodeD),
                edges = listOf(edgeAB, edgeCD),
            )

        assertEquals(setOf(edgeAB, edgeCD), map.edges.toSet())
    }

    @Test
    fun `allows a cycle`() {
        val edgeBA = edge(202, nodeB, nodeA)

        val map = createMap(edges = listOf(edgeAB, edgeBA))

        assertEquals(setOf(edgeAB, edgeBA), map.edges.toSet())
    }

    @Test
    fun `copies the input node collection`() {
        val inputNodes = mutableListOf(nodeA, nodeB)
        val map = createMap(nodes = inputNodes)

        inputNodes.clear()

        assertEquals(setOf(nodeA, nodeB), map.nodes.toSet())
    }

    @Test
    fun `copies the input edge collection`() {
        val inputEdges = mutableListOf(edgeAB)
        val map = createMap(edges = inputEdges)

        inputEdges.clear()

        assertEquals(setOf(edgeAB), map.edges.toSet())
    }

    @Test
    fun `finds a node by identifier`() {
        val map = createMap()

        assertEquals(nodeA, map.findNode(nodeA.id))
    }

    @Test
    fun `returns null when a node does not exist`() {
        val map = createMap()

        assertNull(map.findNode(NodeId(uuid(999))))
    }

    @Test
    fun `returns outgoing edges for an existing node`() {
        val edgeAC = edge(202, nodeA, nodeC)
        val edgeCA = edge(203, nodeC, nodeA)
        val map =
            createMap(
                nodes = listOf(nodeA, nodeB, nodeC),
                edges = listOf(edgeAB, edgeAC, edgeCA),
            )

        assertEquals(setOf(edgeAB, edgeAC), map.outgoingEdgesFrom(nodeA.id).toSet())
    }

    @Test
    fun `returns an empty outgoing edge list for a sink node`() {
        val map = createMap()

        assertEquals(emptyList(), map.outgoingEdgesFrom(nodeB.id))
    }

    @Test
    fun `rejects an outgoing edge lookup for an unknown node`() {
        val map = createMap()

        assertFailsWith<IllegalArgumentException> {
            map.outgoingEdgesFrom(NodeId(uuid(999)))
        }
    }

    private fun createMap(
        nodes: Collection<Node> = listOf(nodeA, nodeB),
        edges: Collection<Edge> = listOf(edgeAB),
    ): NavigationMap =
        NavigationMap.create(
            id = mapId,
            name = mapName,
            nodes = nodes,
            edges = edges,
        )

    private fun node(
        identifier: Int,
        xMeters: Double,
        yMeters: Double,
    ): Node =
        Node(
            id = NodeId(uuid(identifier)),
            coordinate = Coordinate(xMeters, yMeters),
        )

    private fun edge(
        identifier: Int,
        from: Node,
        to: Node,
        distanceMeters: Double = 1.0,
    ): Edge =
        Edge(
            id = EdgeId(uuid(identifier)),
            from = from.id,
            to = to.id,
            distanceMeters = distanceMeters,
        )

    private fun uuid(identifier: Int): UUID {
        val suffix = identifier.toString().padStart(12, '0')

        return UUID.fromString("00000000-0000-0000-0000-$suffix")
    }
}
