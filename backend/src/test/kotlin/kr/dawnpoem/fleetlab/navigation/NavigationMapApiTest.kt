package kr.dawnpoem.fleetlab.navigation

import com.jayway.jsonpath.JsonPath
import kr.dawnpoem.fleetlab.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class NavigationMapApiTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val jdbc: JdbcTemplate,
    ) {
        @Test
        fun `creates a snapshot and retrieves it by generated UUID`() {
            val result =
                mvc
                    .perform(post("/api/maps").contentType(MediaType.APPLICATION_JSON).content(VALID_MAP))
                    .andExpect(status().isCreated)
                    .andExpect(jsonPath("$.name").value("Warehouse"))
                    .andExpect(jsonPath("$.nodes.length()").value(2))
                    .andExpect(jsonPath("$.edges.length()").value(1))
                    .andReturn()
            val body = JsonPath.parse(result.response.contentAsString)
            val id = UUID.fromString(body.read<String>("$.id"))
            val start = UUID.fromString(body.read<String>("$.nodes[0].id"))
            val end = UUID.fromString(body.read<String>("$.nodes[1].id"))
            UUID.fromString(body.read<String>("$.edges[0].id"))
            assertNotEquals(start, end)
            assertEquals(start.toString(), body.read<String>("$.edges[0].from"))
            assertEquals(end.toString(), body.read<String>("$.edges[0].to"))
            assertEquals("/api/maps/$id", result.response.getHeader("Location"))

            mvc
                .perform(get("/api/maps/$id"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Warehouse"))
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges[0].from").value(start.toString()))
                .andExpect(jsonPath("$.edges[0].to").value(end.toString()))
                .andExpect(jsonPath("$.edges[0].distanceMeters").value(2.5))
        }

        @Test
        fun `repeated creation issues independent snapshot identifiers`() {
            fun create(): String =
                mvc
                    .perform(post("/api/maps").contentType(MediaType.APPLICATION_JSON).content(VALID_MAP))
                    .andExpect(status().isCreated)
                    .andReturn()
                    .response.contentAsString

            val first = JsonPath.parse(create())
            val second = JsonPath.parse(create())
            listOf("$.id", "$.nodes[0].id", "$.edges[0].id").forEach {
                assertNotEquals(first.read<String>(it), second.read<String>(it))
            }
        }

        @Test
        fun `returns a problem for an unknown map`() {
            mvc
                .perform(get("/api/maps/${UUID.randomUUID()}"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
        }

        @Test
        fun `rejects a malformed map UUID`() {
            mvc
                .perform(get("/api/maps/not-a-uuid"))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "blankName",
                "duplicateKey",
                "blankKey",
                "unknownKey",
                "selfLoop",
                "zeroDistance",
                "isolated",
                "empty",
                "missingField",
                "malformed",
            ],
        )
        fun `rejects invalid input without storing a map`(kind: String) {
            val input =
                when (kind) {
                    "blankName" -> VALID_MAP.replace(" Warehouse ", " ")
                    "duplicateKey" -> VALID_MAP.replace("\"key\":\"B\"", "\"key\":\"A\"")
                    "blankKey" -> VALID_MAP.replace("\"key\":\"A\"", "\"key\":\"\"")
                    "unknownKey" -> VALID_MAP.replace("\"to\":\"B\"", "\"to\":\"unknown\"")
                    "selfLoop" -> VALID_MAP.replace("\"to\":\"B\"", "\"to\":\"A\"")
                    "zeroDistance" -> VALID_MAP.replace("2.5", "0.0")
                    "isolated" -> VALID_MAP.replace("],\"edges\"", ",{\"key\":\"C\",\"xMeters\":3,\"yMeters\":0}],\"edges\"")
                    "empty" -> """{"name":"Empty","nodes":[],"edges":[]}"""
                    "missingField" -> """{"name":"Missing"}"""
                    else -> "{broken"
                }
            val count = jdbc.queryForObject("select count(*) from navigation_map", Long::class.java)

            mvc
                .perform(post("/api/maps").contentType(MediaType.APPLICATION_JSON).content(input))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))

            assertEquals(count, jdbc.queryForObject("select count(*) from navigation_map", Long::class.java))
        }

        private companion object {
            const val VALID_MAP =
                """{"name":" Warehouse ","nodes":[{"key":"A","xMeters":-1,"yMeters":0},{"key":"B","xMeters":2,"yMeters":0}],"edges":[{"from":"A","to":"B","distanceMeters":2.5}]}"""
        }
    }
