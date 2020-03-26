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

/**
 * An interface for defining the values of an abstract domain commonly used in abstract
 * interpretation frameworks (https://en.wikipedia.org/wiki/Abstract_interpretation).
 */
interface AbstractValue<V: AbstractValue<V>> {
    /** Returns true if [this] is less than [other] in lattice ordering for the abstract domain. */
    infix fun isLessThan(other: V): Boolean

    /**
     * Returns true if the two values are semantically equivalent.
     *
     * Note that this is semantic equivalence and not structural equality. This method is
     * used to determine if we have reached a fix point. For some domains, this may simply
     * be structural equality.
     */
    infix fun isEquivalentTo(other: V): Boolean

    /**
     * Returns true if [this] represents `Bottom`. `Bottom` is the lowest value in the domain
     * lattice and represents an empty set of concrete values.
     */
    fun isBottom(): Boolean

    /**
     * Returns true if [this] represents `Top`. `Top` is the greatest value in the domain lattice
     * and represents the universe of concrete values.
     */
    fun isTop(): Boolean

    /** Returns the least upper bound of the values. */
    infix fun join(other: V): V

    /** Returns the greatest lower bound of the values. */
    infix fun meet(other: V): V

    /**
     * Returns the widened value.
     *
     * A widening operator is used to ensure that the increasing fixpoint computation terminates for
     * domains with infinite ascending chains in its lattice or even to accelerate fixpoint
     * computations (at the cost of precision) for domains with no infinite ascending chains. For
     * lattices of finite height, this can be the same as join.
     *
     * Also, note that the widen operator is not commutative.
     */
    infix fun widen(other: V) = (this join other)

    /**
     * Returns the narrowed value.
     *
     * A narrowing operator is the dual of widen and used to ensure that decreasing fixpoint
     * computation terminates for domains with lattices of infinite height. For lattices of finite
     * height, this can be the same as meet.
     *
     * Also, note that the widen operator is not commutative.
     */
    infix fun narrow(other: V) = (this meet other)
}
