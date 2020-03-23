package arcs.core.analysis

import arcs.core.data.EntityType
import arcs.core.data.FieldType
import arcs.core.data.Schema
import arcs.core.data.SchemaFields
import arcs.core.data.SchemaName
import arcs.core.testutil.assertThrows
import arcs.core.util.Result
import arcs.core.util.getOrNull
import arcs.core.util.getOrThrow
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EntityTypeUnionTest {
    @Test
    fun schemaUnionMergesNames() {
        val emptySchemaFields = SchemaFields(emptyMap(), emptyMap())
        // TODO(bgogul): hash
        val thingSchema = Schema(
            listOf(SchemaName("Thing"), SchemaName("Another")),
            emptySchemaFields,
            ""
        )
        val objectSchema = Schema(
            listOf(SchemaName("Object"), SchemaName("Thing")),
            emptySchemaFields,
            ""
        )
        val thingObjectSchema = thingSchema.union(objectSchema).getOrNull()
        assertThat(thingObjectSchema).isNotNull()
        thingObjectSchema?.let {
            assertThat(it.names).containsExactly(
                SchemaName("Thing"),
                SchemaName("Object"),
                SchemaName("Another")
            )
        }
    }

    @Test
    fun schemaUnionMergesSingletons() {
        val textField = SchemaFields(
            mapOf("text" to FieldType.Text),
            emptyMap()
        )
        val numberField = SchemaFields(
            mapOf("number" to FieldType.Number),
            emptyMap()
        )
        // TODO(bgogul): hash
        val textSchema = Schema(listOf(SchemaName("Example")), textField, "")
        val numberSchema = Schema(listOf(SchemaName("Example")), numberField, "")
        val result = textSchema.union(numberSchema).getOrNull()
        result?.let {
            assertThat(it.names).containsExactly(SchemaName("Example"))
            assertThat(it.fields.singletons).isEqualTo(
                mapOf("text" to FieldType.Text, "number" to FieldType.Number)
            )
        }
    }

    @Test
    fun schemaUnionDetectsIncompatibleFieldTypes() {
        val textField = SchemaFields(
            mapOf("num_text" to FieldType.Text),
            emptyMap()
        )
        val numberField = SchemaFields(
            mapOf("num_text" to FieldType.Number),
            emptyMap()
        )
        // TODO(bgogul): hash
        val textSchema = Schema(listOf(SchemaName("Example")), textField, "")
        val numberSchema = Schema(listOf(SchemaName("Example")), numberField, "")
        val exception = assertThrows(IllegalArgumentException::class) {
            textSchema.union(numberSchema).getOrThrow()
        }
        assertThat(exception).hasMessageThat().contains("Incompatible types for field 'num_text'")
    }

    @Test
    fun entityTypeUnionComputesUnionOfSchemas() {
        val textField = SchemaFields(
            mapOf("text" to FieldType.Text),
            emptyMap()
        )
        val numberField = SchemaFields(
            mapOf("number" to FieldType.Number),
            emptyMap()
        )
        // TODO(bgogul): hash
        val textSchema = Schema(listOf(SchemaName("Example")), textField, "")
        val numberSchema = Schema(listOf(SchemaName("Example")), numberField, "")
        val textEntity = EntityType(textSchema)
        val numberEntity = EntityType(numberSchema)
        val result = textEntity.union(numberEntity).getOrNull()
        result?.let {
            assertThat(it.entitySchema).isEqualTo(numberSchema.union(textSchema).getOrNull())
        }
    }
}
