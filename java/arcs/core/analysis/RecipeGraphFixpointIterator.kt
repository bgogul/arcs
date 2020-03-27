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

/** An interface for implementing dataflow analysis over abstract values of type [V]. */
open class RecipeGraphFixpointIterator<V: AbstractValue<V>> {
    /** Abstract state at the node, i.e., the join of the abstract values along the input edges. */
    var nodeValues = mutableMapOf<RecipeGraph.Node, V>()

    // TODO: Add a getState and also make nodeValues private
    // TODO: What are the Implications of Bottom, top, etc.?
    // fun getState(node: FlowGraph.Node) =  nodeValues.getOrDefault(node, V())

    /** State transformer for a [Recipe.Handle] node. */
    open fun transform(handle: Recipe.Handle, input: V): V = input

    /** State transformer for a [Recipe.Particle] node. */
    open fun transform(particle: Recipe.Particle, input: V): V = input

    /** State transformer for a [Recipe.Handle] -> [Recipe.Particle] edge. */
    open fun transform(
        handle: Recipe.Handle,
        particle: Recipe.Particle,
        spec: HandleConnectionSpec,
        input: V
    ): V = input

    /** State transformer for a [Recipe.Particle] -> [Recipe.Handle] edge. */
    open fun transform(
        particle: Recipe.Particle,
        handle: Recipe.Handle,
        spec: HandleConnectionSpec,
        input: V
    ): V = input

    /**
     * Initializes the fixpoint iterator for the given graph.
     *
     * For instance this method may be used to seed the initial values for the nodes in the graph.
     */
    open fun initialize(graph: RecipeGraph): Set<RecipeGraph.Node> = graph.nodes.toSet()

    fun computeFixpoint(graph: RecipeGraph) {
        // TODO: Need to resolve cases where we can have identical particles in recipes.
        val worklist = mutableSetOf<RecipeGraph.Node>()
        worklist.addAll(initialize(graph))
        while (worklist.isNotEmpty()) {
            // Pick and remove an element from the worklist.
            val current = worklist.first()
            worklist.remove(current)
            val input = nodeValues[current]
            // TODO(bgogul): Treat this as an invalid state?
            if (input == null) continue
            val output = transform(current, input)
            current.successors.forEach { (succ, spec) ->
                val edgeValue = transform(current, succ, spec, output)
                val oldValue = nodeValues[succ]
                if (oldValue == null) {
                    // TODO(bgogul): need to set non-existing value to bottom.
                    worklist.add(succ)
                    nodeValues[succ] = edgeValue
                } else {
                    val newValue = oldValue.join(output)
                    if (!newValue.isEquivalentTo(oldValue)) {
                        worklist.add(succ)
                        nodeValues[succ] = newValue
                    }
                }
            }
        }
    }

    private fun transform(node: RecipeGraph.Node, input: V) =  when (node) {
        is RecipeGraph.Node.Particle -> transform(node.particle, input)
        is RecipeGraph.Node.Handle -> transform(node.handle, input)
    }

    private fun transform(
        src: RecipeGraph.Node,
        tgt: RecipeGraph.Node,
        spec: HandleConnectionSpec,
        input: V
    ) = when (src) {
        is RecipeGraph.Node.Particle -> {
            require(tgt is RecipeGraph.Node.Handle)
            transform(src.particle, tgt.handle, spec, input)
        }
        is RecipeGraph.Node.Handle -> {
            require(tgt is RecipeGraph.Node.Particle)
            transform(src.handle, tgt.particle, spec, input)
        }
    }
}
