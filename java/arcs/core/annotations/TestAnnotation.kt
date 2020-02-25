package arcs.core.annotations

@Particle class HelloWorld: arcs.sdk.BaseParticle() {
    override fun getTemplate(slotName: String) = "<b>Hello, world!</b>"
}

fun main(args: Array<String>) {
    println("Hello world!")
   //println(Generated_Santa().greeting())
}
