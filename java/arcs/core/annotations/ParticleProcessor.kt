package arcs.core.annotations

import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation


//@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
//@SupportedOptions(ParticleProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)

@AutoService(Processor::class)
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
        processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/${className}.arcs")
                .openWriter().use {
                    it.write(
                            """
                    package ${className}
                    class ${className}Generated
                """.trimIndent()
                    )
                }
//        val x = StandardLocation.CLASS_OUTPUT.getName()
//        println("${x}")
//        println(processingEnv.options.toString())
//        val fileName = "Generated_$className"
//        val fileContent = ManifestBuilder(fileName,pack).getContent()
//        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
//        println("${KAPT_KOTLIN_GENERATED_OPTION_NAME}")
//        println("${processingEnv.options}")
//        val file = File(kaptKotlinGeneratedDir, "$fileName.kt")
//        println("Generate class ${className}: ${kaptKotlinGeneratedDir}")
//
//        file.writeText(fileContent)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}