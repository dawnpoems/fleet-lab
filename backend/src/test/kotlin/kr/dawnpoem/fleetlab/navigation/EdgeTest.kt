package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EdgeTest {
    private val edgeId = EdgeId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val from = NodeId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
    private val to = NodeId(UUID.fromString("00000000-0000-0000-0000-000000000003"))

    @Test
    fun `accepts distinct nodes and a positive finite distance`() {
        val edge = Edge(
            id = edgeId,
            from = from,
            to = to,
            distanceMeters = 12.5,
        )

        assertEquals(edgeId, edge.id)
        assertEquals(from, edge.from)
        assertEquals(to, edge.to)
        assertEquals(12.5, edge.distanceMeters)
    }

    @Test
    fun `rejects a zero distance`() {
        assertFailsWith<IllegalArgumentException> {
            edgeWith(distanceMeters = 0.0)
        }
    }

    @Test
    fun `rejects a negative distance`() {
        assertFailsWith<IllegalArgumentException> {
            edgeWith(distanceMeters = -0.1)
        }
    }

    @Test
    fun `rejects a NaN distance`() {
        assertFailsWith<IllegalArgumentException> {
            edgeWith(distanceMeters = Double.NaN)
        }
    }

    @Test
    fun `rejects infinite distances`() {
        listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> {
                edgeWith(distanceMeters = value)
            }
        }
    }

    @Test
    fun `rejects a self-loop`() {
        assertFailsWith<IllegalArgumentException> {
            Edge(
                id = edgeId,
                from = from,
                to = from,
                distanceMeters = 1.0,
            )
        }
    }

    private fun edgeWith(distanceMeters: Double): Edge =
        Edge(
            id = edgeId,
            from = from,
            to = to,
            distanceMeters = distanceMeters,
        )
}
