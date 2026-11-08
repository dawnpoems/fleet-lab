package kr.dawnpoem.fleetlab.navigation

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import kr.dawnpoem.fleetlab.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class NavigationPathApiTest
    @Autowired
    constructor(
        private val environment: Environment,
    ) {
        private val client = HttpClient.newHttpClient()

        @Test
        fun `creates retrieves and finds an ordered path over real HTTP`() {
            assertEquals(200, request("/actuator/health").statusCode())
            val map = create()
            val id = map.read<String>("$.id")
            assertEquals(200, request("/api/maps/$id").statusCode())

            val response = path(map, node(map, 0), node(map, 2))
            assertEquals(200, response.statusCode())
            val body = JsonPath.parse(response.body())
            assertEquals("Found", body.read<String>("$.status"))
            assertEquals(node(map, 0), body.read<String>("$.path.start"))
            assertEquals(node(map, 2), body.read<String>("$.path.destination"))
            assertEquals((0..2).map { node(map, it) }, body.read<List<String>>("$.path.nodeIds"))
            assertEquals(map.read<List<String>>("$.edges[*].id"), body.read<List<String>>("$.path.edgeIds"))
            assertEquals(4.0, body.read<Double>("$.path.totalDistanceMeters"))
        }

        @Test
        fun `returns an empty path for the same node`() {
            val map = create()
            val response = path(map, node(map, 0), node(map, 0))
            assertEquals(200, response.statusCode())
            val body = JsonPath.parse(response.body())
            assertEquals("Found", body.read<String>("$.status"))
            assertEquals(emptyList(), body.read<List<String>>("$.path.edgeIds"))
            assertEquals(listOf(node(map, 0)), body.read<List<String>>("$.path.nodeIds"))
            assertEquals(0.0, body.read<Double>("$.path.totalDistanceMeters"))
        }

        @Test
        fun `returns unreachable as a normal response`() {
            val map = create()
            val response = path(map, node(map, 2), node(map, 0))
            assertEquals(200, response.statusCode())
            val body = JsonPath.parse(response.body())
            assertEquals("Unreachable", body.read<String>("$.status"))
            assertFalse(body.read<Map<String, Any>>("$").containsKey("path"))
        }

        @ParameterizedTest
        @ValueSource(strings = ["start", "destination", "both"])
        fun `rejects unknown nodes including identical unknown identifiers`(which: String) {
            val map = create()
            val unknown = UUID.randomUUID().toString()
            val start = if (which == "destination") node(map, 0) else unknown
            val destination = if (which == "start") node(map, 2) else unknown
            assertProblem(path(map, start, destination), 400)
        }

        @Test
        fun `returns not found for an unknown map`() {
            val response =
                request(
                    "/api/maps/${UUID.randomUUID()}/paths",
                    """{"start":"${UUID.randomUUID()}","destination":"${UUID.randomUUID()}"}""",
                )
            assertProblem(response, 404)
        }

        @ParameterizedTest
        @ValueSource(strings = ["{}", "{broken", """{"start":"invalid","destination":"invalid"}"""])
        fun `rejects malformed path requests`(input: String) {
            val map = create()
            assertProblem(request("/api/maps/${map.read<String>("$.id")}/paths", input), 400)
        }

        @Test
        fun `reports numeric overflow separately from unreachable`() {
            val map = create(MAP.replace("1.25", "1.7976931348623157E308").replace("2.75", "1.7976931348623157E308"))
            assertProblem(path(map, node(map, 0), node(map, 2)), 422)
        }

        private fun create(input: String = MAP): DocumentContext {
            val response = request("/api/maps", input)
            assertEquals(201, response.statusCode(), response.body())
            return JsonPath.parse(response.body())
        }

        private fun node(
            map: DocumentContext,
            index: Int,
        ): String = map.read("$.nodes[$index].id")

        private fun path(
            map: DocumentContext,
            start: String,
            destination: String,
        ): HttpResponse<String> =
            request(
                "/api/maps/${map.read<String>("$.id")}/paths",
                """{"start":"$start","destination":"$destination"}""",
            )

        private fun request(
            path: String,
            body: String? = null,
        ): HttpResponse<String> {
            val port = environment.getRequiredProperty("local.server.port")
            val builder = HttpRequest.newBuilder(URI.create("http://localhost:$port$path")).timeout(Duration.ofSeconds(10))
            if (body != null) {
                builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body))
            }
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        }

        private fun assertProblem(
            response: HttpResponse<String>,
            status: Int,
        ) {
            assertEquals(status, response.statusCode(), response.body())
            assertTrue(
                response
                    .headers()
                    .firstValue("Content-Type")
                    .orElse("")
                    .startsWith("application/problem+json"),
            )
            assertEquals(status, JsonPath.parse(response.body()).read<Int>("$.status"))
        }

        private companion object {
            val MAP =
                """
                {
                  "name":"Path demo",
                  "nodes":[
                    {"key":"A","xMeters":0,"yMeters":0},
                    {"key":"B","xMeters":1,"yMeters":0},
                    {"key":"C","xMeters":2,"yMeters":0}
                  ],
                  "edges":[
                    {"from":"A","to":"B","distanceMeters":1.25},
                    {"from":"B","to":"C","distanceMeters":2.75}
                  ]
                }
                """.trimIndent()
        }
    }
