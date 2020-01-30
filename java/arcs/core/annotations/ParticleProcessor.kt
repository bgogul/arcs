package arcs.core.annotations

import com.google.auto.service.AutoService
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation


@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@AutoService(Processor::class)
@SupportedOptions(ParticleProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ParticleProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        println("annotation")
        return mutableSetOf(Particle::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        println("Supported")
        return SourceVersion.latest()
    }


    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        println("process")
        roundEnvironment?.getElementsAnnotatedWith(Particle::class.java)
                ?.forEach {
                    val className = it.simpleName.toString()
                    val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                    generateClass(className, pack)
                }
        return true
    }

    private fun generateClass(className: String, pack: String) {
        val x = StandardLocation.CLASS_OUTPUT.getName()

        println(processingEnv.options.toString())
        val fileName = "Generated_$className"
        val fileContent = ManifestBuilder(fileName,pack).getContent()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$fileName.kt")
        println("Generate class ${className}: ${kaptKotlinGeneratedDir}")

        file.writeText(fileContent)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}