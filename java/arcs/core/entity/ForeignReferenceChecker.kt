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
package arcs.core.entity

import arcs.core.data.Schema
import arcs.core.storage.Reference as StorageReference
import arcs.core.storage.keys.ForeignStorageKey

/**
 * Interface used to check the validity of external references. Checks are associated with a
 * namespace, represented by an empty Arcs schema. A given id in this namespace can be checked for
 * external validity.
 */
interface ForeignReferenceChecker {
    fun check(namespace: Schema, value: String): Boolean
}

/**
 * Implementation of ForeignReferenceChecker, based on the provided map of checks.
 */
class ForeignReferenceCheckerImpl(
    private val validityChecks: Map<Schema, (String) -> Boolean>
) : ForeignReferenceChecker {

    init {
        validityChecks.keys.forEach { checkNoFields(it) }
    }

    // Checks the given value using the checker for the given namespace.
    override fun check(namespace: Schema, value: String): Boolean =
        checkNotNull(validityChecks[namespace]) {
            "Foreign type not registered: $namespace."
        }.invoke(value)

    private fun checkNoFields(schema: Schema) {
        check(schema.fields.singletons.isEmpty() && schema.fields.collections.isEmpty()) {
            "Only empty schemas can be used for foreign references."
        }
    }
}

/**
 * Create a foreign [Reference] of type [T] with the given [id]. It does not check for validity of
 * that [id].
 *
 * Note: this is a temporary method, this functionality will be part of the EntityHandle when we
 * have one and it is used to create references.
 */
fun <T : Entity> foreignReference(spec: EntitySpec<T>, id: String): Reference<T> {
    return Reference(
        spec,
        StorageReference(id, storageKey = ForeignStorageKey(spec.SCHEMA), version = null)
    )
}
