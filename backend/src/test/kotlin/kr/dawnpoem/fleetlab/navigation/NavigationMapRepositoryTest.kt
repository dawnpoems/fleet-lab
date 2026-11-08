package kr.dawnpoem.fleetlab.navigation

import jakarta.persistence.EntityManager
import kr.dawnpoem.fleetlab.TestcontainersConfiguration
import kr.dawnpoem.fleetlab.navigation.port.NavigationMapRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class NavigationMapRepositoryTest
    @Autowired
    constructor(
        private val repository: NavigationMapRepository,
        private val entityManager: EntityManager,
        transactionManager: PlatformTransactionManager,
    ) {
        private val transaction = TransactionTemplate(transactionManager)

        @Test
        fun `restores the complete graph from the database`() {
            val map = fixture()

            repository.save(map)

            assertGraphEquals(map, reload(map.id))
        }

        @Test
        fun `returns null for an unknown map`() {
            assertNull(reload(NavigationMapId(UUID.randomUUID())))
        }

        @Test
        fun `rejects saving the identical snapshot twice`() {
            val map = fixture()
            repository.save(map)

            assertFailsWith<DataIntegrityViolationException> { repository.save(map) }

            assertGraphEquals(map, reload(map.id))
        }

        @Test
        fun `rejects overwriting an existing map with different contents`() {
            val original = fixture()
            repository.save(original)
            val replacement = fixture(id = original.id, name = "Replacement")

            assertFailsWith<DataIntegrityViolationException> { repository.save(replacement) }

            assertGraphEquals(original, reload(original.id))
        }

        @Test
        fun `keeps independent snapshots separate`() {
            val first = fixture()
            val second = fixture()
            repository.save(first)
            repository.save(second)

            assertGraphEquals(first, reload(first.id))
            assertGraphEquals(second, reload(second.id))
        }

        @Test
        fun `preserves parallel edges and direction`() {
            val original = fixture()
            val parallel = original.edges.first().copy(id = EdgeId(UUID.randomUUID()))
            val map =
                NavigationMap.create(original.id, original.name, original.nodes, original.edges + parallel)
            repository.save(map)

            val restored = assertNotNull(reload(map.id))

            assertEquals(map.edges.toSet(), restored.edges.toSet())
            assertEquals(
                PathPlanningResult.Unreachable,
                DijkstraPathPlanner().findPath(restored, map.nodes.last().id, map.nodes.first().id),
            )
        }

        @Test
        fun `preserves deterministic shortest path after reloading`() {
            val map = fixture()
            val planner = DijkstraPathPlanner()
            val expected = planner.findPath(map, map.nodes.first().id, map.nodes.last().id)
            repository.save(map)

            val restored = assertNotNull(reload(map.id))

            assertEquals(expected, planner.findPath(restored, map.nodes.first().id, map.nodes.last().id))
        }

        @Test
        fun `rolls back the whole snapshot when a node identifier is already stored`() {
            val original = fixture()
            repository.save(original)
            val conflicting =
                NavigationMap.create(
                    NavigationMapId(UUID.randomUUID()),
                    original.name,
                    original.nodes,
                    original.edges.map { it.copy(id = EdgeId(UUID.randomUUID())) },
                )

            assertFailsWith<DataIntegrityViolationException> { repository.save(conflicting) }

            assertNull(reload(conflicting.id))
            assertGraphEquals(original, reload(original.id))
        }

        private fun reload(id: NavigationMapId): NavigationMap? =
            transaction.execute {
                entityManager.flush()
                entityManager.clear()
                repository.findById(id)
            }

        private fun assertGraphEquals(
            expected: NavigationMap,
            actual: NavigationMap?,
        ) {
            val restored = assertNotNull(actual)
            assertEquals(expected.id, restored.id)
            assertEquals(expected.name, restored.name)
            assertEquals(expected.nodes.toSet(), restored.nodes.toSet())
            assertEquals(expected.edges.toSet(), restored.edges.toSet())
        }

        private fun fixture(
            id: NavigationMapId = NavigationMapId(UUID.randomUUID()),
            name: String = "창고 Map",
        ): NavigationMap {
            val nodes =
                (0..3).map {
                    Node(NodeId(UUID.randomUUID()), Coordinate(it.toDouble() - 1.5, -it.toDouble()))
                }
            val edges =
                listOf(
                    edge(nodes[0], nodes[1]),
                    edge(nodes[1], nodes[3]),
                    edge(nodes[0], nodes[2]),
                    edge(nodes[2], nodes[3]),
                )
            return NavigationMap.create(id, NavigationMapName.of(name), nodes, edges)
        }

        private fun edge(
            from: Node,
            to: Node,
        ): Edge = Edge(EdgeId(UUID.randomUUID()), from.id, to.id, 1.25)
    }
