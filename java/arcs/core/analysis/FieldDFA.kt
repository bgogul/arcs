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

import arcs.core.data.HandleConnectionSpec
import arcs.core.data.Recipe

// TODO(bgogul): Quickly hacked up together. Think about proper representaton for bottom, top, etc.
class FieldDomain(
    val fieldLabels: Map<String, LoggableDomain> = emptyMap()
) : AbstractValue<FieldDomain> {
    override infix fun isLessThan(other: FieldDomain) = false // TODO
    override infix fun isEquivalentTo(other: FieldDomain) = fieldLabels == other.fieldLabels
    override fun isBottom() = false // TODO
    override fun isTop() = false // TODO
    override infix fun join(other: FieldDomain) = FieldDomain(
        fieldLabels.mergeReduce(other.fieldLabels) { a, b -> a join b }
    )
    override infix fun meet(other: FieldDomain) = this // TODO

    private fun <K, V> Map<K, V>.mergeReduce(
        other: Map<K, V>, reduce: (V, V) -> V = { a, b -> b }
    ): Map<K, V> {
        val result = mutableMapOf<K, V>()
        result.putAll(this)
        for ((key, value) in other) {
            result[key] = result[key]?.let { reduce(value, it) } ?: value
        }
        return result
    }

    fun minus(name: String, label: LoggableDomain.Label): FieldDomain {
        if (fieldLabels[name] == null) return this
        var result = fieldLabels.toMutableMap()
        result.put(name, result[name]?.let { it - label }!!) // TODO: remove !!
        return FieldDomain(result)
    }
    fun plus(name: String, label: LoggableDomain.Label): FieldDomain {
        if (fieldLabels[name] == null) return this
        var result = fieldLabels.toMutableMap()
        result.put(name, result[name]?.let { it + label }!!) // TODO: remove !!
        return FieldDomain(result)
    }

    override fun toString() = fieldLabels.map {  (name, labels) -> "$name -> $labels" }.joinToString()
}

class FieldAnalysis : RecipeGraphFixpointIterator<FieldDomain>() {

    override fun transform(
        particle: Recipe.Particle,
        handle: Recipe.Handle,
        spec: HandleConnectionSpec,
        input: FieldDomain
    ): FieldDomain {
        if (particle.spec.name != REDACTOR_PARTICLE || spec.name != REDACTOR_CONNECTION) {
            return input
        }
        return input
            .minus("name", LoggableDomain.Label.NOT_LOGGABLE)
            .plus("name", LoggableDomain.Label.LOGGABLE)
    }

    override fun initialize(graph: RecipeGraph): Set<RecipeGraph.Node> {
        return graph.nodes.mapNotNull mapNodes@{ node ->
            if (node !is RecipeGraph.Node.Particle) return@mapNodes null
            if (node.particle.spec.name != CREATOR_PARTICLE) return@mapNodes null
            // TODO: This should be filled in from type inference.=
            nodeValues[node] = FieldDomain(
                mapOf(
                    "name" to LoggableDomain(setOf(LoggableDomain.Label.NOT_LOGGABLE)),
                    "age" to LoggableDomain(setOf(LoggableDomain.Label.NOT_LOGGABLE))
            ))
            return@mapNodes node
        }.toSet()
    }

    companion object {
        private val CREATOR_PARTICLE = "Creator"
        private val REDACTOR_PARTICLE = "Redactor"
        private val REDACTOR_CONNECTION = "redacted"
    }
}
