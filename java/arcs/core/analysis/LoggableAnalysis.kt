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
class LoggableDomain(val labels: Set<Label> = emptySet()) : AbstractValue<LoggableDomain> {
    enum class Label {
        LOGGABLE,
        NOT_LOGGABLE
    }
    override infix fun isLessThan(other: LoggableDomain) = other.labels.containsAll(labels)
    override infix fun isEquivalentTo(other: LoggableDomain) = labels == other.labels
    override fun isBottom() = false // TODO
    override fun isTop() = false // TODO
    override infix fun join(other: LoggableDomain) = LoggableDomain(labels union other.labels)
    override infix fun meet(other: LoggableDomain) = LoggableDomain(labels intersect other.labels)

    operator fun minus(label: Label) = LoggableDomain(labels - label)
    operator fun plus(label: Label) = LoggableDomain(labels + label)

    override fun toString() = "$labels"
}

class LoggableAnalysis : RecipeGraphFixpointIterator<LoggableDomain>() {

    override fun transform(
        particle: Recipe.Particle,
        handle: Recipe.Handle,
        spec: HandleConnectionSpec,
        input: LoggableDomain
    ): LoggableDomain {
        if (particle.spec.name != REDACTOR_PARTICLE || spec.name != REDACTOR_CONNECTION) {
            return input
        }
        return (input - LoggableDomain.Label.NOT_LOGGABLE) + LoggableDomain.Label.LOGGABLE
    }

    override fun initialize(graph: RecipeGraph): Set<RecipeGraph.Node> {
        return graph.nodes.mapNotNull mapNodes@{ node ->
            if (node !is RecipeGraph.Node.Particle) return@mapNodes null
            if (node.particle.spec.name != CREATOR_PARTICLE) return@mapNodes null
            nodeValues[node] = LoggableDomain(setOf(LoggableDomain.Label.NOT_LOGGABLE))
            return@mapNodes node
        }.toSet()
    }

    companion object {
        private val CREATOR_PARTICLE = "Creator"
        private val REDACTOR_PARTICLE = "Redactor"
        private val REDACTOR_CONNECTION = "redacted"
    }
}
