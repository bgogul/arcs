package arcs.core.analysis

import arcs.core.data.proto.RecipeEnvelopeProto
import arcs.core.data.proto.decodeRecipe
import arcs.core.data.Recipe
import arcs.repoutils.runfilesDir
import com.google.protobuf.TextFormat
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RecipeGraphUtilsTest {
    @Test
    fun dumpGraph() {
        val path = runfilesDir() + "java/arcs/core/data/testdata/example.textproto"
        val builder = RecipeEnvelopeProto.newBuilder()
        TextFormat.getParser().merge(File(path).readText(), builder)
        val recipeEnvelopeProto: RecipeEnvelopeProto = builder.build()
        val recipe: Recipe = recipeEnvelopeProto.decodeRecipe()
        val graph = RecipeGraph(recipe)
        print (graph.toDotGraph {_ -> ""})
    }
}
