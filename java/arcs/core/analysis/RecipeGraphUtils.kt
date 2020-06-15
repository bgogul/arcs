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

/** Returns a dot representation of the [RecipeGraph]. */
fun RecipeGraph.toDotGraph(nodeLabeler: (RecipeGraph.Node) -> String): String {
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
    val dotNodeNames = IdentityHashMap<RecipeGraph.Node, String>()
    nodes.forEach {
        dotNodeNames[it] = when (it) {
            is RecipeGraph.Node.Particle -> getUniqueName(it.particle)
            is RecipeGraph.Node.Handle -> "${it.handle.name}"
        }
    }
    val dotNodes = dotNodeNames.map { (node, name) ->
        when (node) {
            is RecipeGraph.Node.Particle -> {
                val nodeLabel = "$name: ${nodeLabeler(node)}"
                """  ${name}[shape="box", label="$nodeLabel"];"""
            }
            is RecipeGraph.Node.Handle -> {
                val typeString = node.handle.type.toString(toStringOptions)
                val nodeLabel = "$name: $typeString ${nodeLabeler(node)}"
                """  ${name}[label="$name: ${nodeLabel}"];"""
            }
        }
    }.joinToString(separator = "\n")
    val dotEdges = nodes.flatMap { node ->
        node.successors.map { (succ, kind) ->
            when (kind) {
                is RecipeGraph.EdgeKind.HandleConnection -> {
                    val typeString = kind.spec.type.toString(toStringOptions)
                    """  ${dotNodeNames[node]} -> ${dotNodeNames[succ]}[label="${typeString}"];"""
                }
                is RecipeGraph.EdgeKind.JoinConnection -> {
                    val componentString = "${kind.spec.component}"
                    """  ${dotNodeNames[node]} -> ${dotNodeNames[succ]}[label="${componentString}"];"""
                }

            }
        }
    }.joinToString(separator = "\n")
    return "digraph G {\n$dotNodes\n$dotEdges\n}"
}
