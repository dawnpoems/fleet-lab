package kr.dawnpoem.fleetlab.navigation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigationMapNameTest {
    @Test
    fun `accepts a valid name`() {
        val name = NavigationMapName.of("Main Warehouse")

        assertEquals("Main Warehouse", name.value)
    }

    @Test
    fun `trims leading and trailing whitespace`() {
        val name = NavigationMapName.of("  Main Warehouse  ")

        assertEquals("Main Warehouse", name.value)
    }

    @Test
    fun `rejects an empty name`() {
        assertFailsWith<IllegalArgumentException> {
            NavigationMapName.of("")
        }
    }

    @Test
    fun `rejects a whitespace-only name`() {
        assertFailsWith<IllegalArgumentException> {
            NavigationMapName.of("   ")
        }
    }

    @Test
    fun `accepts a 100-character name`() {
        val value = "a".repeat(100)

        assertEquals(value, NavigationMapName.of(value).value)
    }

    @Test
    fun `rejects a 101-character name`() {
        assertFailsWith<IllegalArgumentException> {
            NavigationMapName.of("a".repeat(101))
        }
    }
}
