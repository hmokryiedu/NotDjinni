package not.djinni.presentation.router.routes.template.mapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import not.djinni.model.template.Template
import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateMapperTest {

    @Test
    fun `single template serializes id and message`() {
        val response = Template(id = 1, message = "Hello").toResponse()

        val jsonObject = json.parseToJsonElement(json.encodeToString(response)).jsonObject

        assertEquals("1", jsonObject["id"]?.jsonPrimitive?.content)
        assertEquals("Hello", jsonObject["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `template list serializes templates wrapper`() {
        val response = listOf(
            Template(id = 2, message = "Second"),
            Template(id = 1, message = "First"),
        ).toResponse()

        val jsonObject = json.parseToJsonElement(json.encodeToString(response)).jsonObject

        assertEquals(2, jsonObject["templates"]?.jsonArray?.size)
        assertEquals("Second", jsonObject["templates"]?.jsonArray?.get(0)?.jsonObject?.get("message")?.jsonPrimitive?.content)
    }

    @Test
    fun `empty template list serializes empty templates wrapper`() {
        val response = emptyList<Template>().toResponse()

        val jsonObject = json.parseToJsonElement(json.encodeToString(response)).jsonObject

        assertEquals(0, jsonObject["templates"]?.jsonArray?.size)
    }

    private companion object {
        val json = Json { explicitNulls = false }
    }
}
