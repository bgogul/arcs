/*
 * Copyright 2020 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */

package arcs.core.analysis

import arcs.core.data.Recipe
import arcs.core.type.Type
import java.util.IdentityHashMap

/** Returns a dot representation of the [FlowGraph]. */
fun FlowGraph.toDotGraph(): String {
    val toStringOptions = Type.ToStringOptions(hideFields = false, pretty = true)
    var particleIndices = mutableMapOf<String, Int>()
    val getUniqueName = { particle: Recipe.Particle ->
        val name = particle.spec.name
        val index = particleIndices[name] ?: 0
        particleIndices[name] = index + 1
        "${name}_${index}"
    }
    // We use [IdentityHashMap] instead of a [MutableMap] or [associateBy] because a [Recipe] can
    // have multiple instances of the same [Recipe.Particle].
    val dotNodeNames = IdentityHashMap<FlowGraph.Node, String>()
    nodes.forEach {
        dotNodeNames[it] = when (it) {
            is FlowGraph.Node.Particle -> getUniqueName(it.particle)
            is FlowGraph.Node.Handle -> "${it.handle.name}"
        }
    }
    val dotNodes = dotNodeNames.map { (node, name) ->
        when (node) {
            is FlowGraph.Node.Particle -> """  ${name}[shape="box"];"""
            is FlowGraph.Node.Handle -> {
                val typeString = node.handle.type.toString(toStringOptions)
                """  ${name}[label="$name: ${typeString}"];"""
            }
        }
    }.joinToString(separator = "\n")
    val dotEdges = nodes.flatMap { node ->
        node.successors.map { (succ, spec) ->
            val typeString = spec.type.toString(toStringOptions)
            """  ${dotNodeNames[node]} -> ${dotNodeNames[succ]}[label="${typeString}"];"""
        }
    }.joinToString(separator = "\n")
    return "digraph G {\n$dotNodes\n$dotEdges\n}"
}
