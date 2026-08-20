package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoordinateTest {
    @Test
    fun `accepts positive zero and negative values`() {
        val coordinate =
            Coordinate(
                xMeters = 12.5,
                yMeters = -3.75,
            )
        val origin =
            Coordinate(
                xMeters = 0.0,
                yMeters = 0.0,
            )

        assertEquals(12.5, coordinate.xMeters)
        assertEquals(-3.75, coordinate.yMeters)
        assertEquals(0.0, origin.xMeters)
        assertEquals(0.0, origin.yMeters)
    }

    @Test
    fun `rejects NaN for x coordinate`() {
        assertFailsWith<IllegalArgumentException> {
            Coordinate(
                xMeters = Double.NaN,
                yMeters = 0.0,
            )
        }
    }

    @Test
    fun `rejects NaN for y coordinate`() {
        assertFailsWith<IllegalArgumentException> {
            Coordinate(
                xMeters = 0.0,
                yMeters = Double.NaN,
            )
        }
    }

    @Test
    fun `rejects infinite x coordinates`() {
        listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> {
                Coordinate(
                    xMeters = value,
                    yMeters = 0.0,
                )
            }
        }
    }

    @Test
    fun `rejects infinite y coordinates`() {
        listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> {
                Coordinate(
                    xMeters = 0.0,
                    yMeters = value,
                )
            }
        }
    }
}
