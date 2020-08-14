/**
 * @license
 * Copyright 2019 Google LLC.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * Code distributed by Google as part of this project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */

// String-based enums.
// TODO: convert to actual enums so that they can be iterated over.

export type Direction = 'reads' | 'writes' | 'reads writes' | 'hosts' | '`consumes' | '`provides' | 'any';
