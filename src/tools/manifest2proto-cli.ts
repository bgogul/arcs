/**
 * @license
 * Copyright (c) 2020 Google Inc. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 * Code distributed by Google as part of this project is also
 * subject to an additional IP rights grant found at
 * http://polymer.github.io/PATENTS.txt
 */
import * as minimist from 'minimist.js';
import * as fs from 'fs.js';
import * as path from 'path.js';
import {Runtime} from '../runtime/runtime.js';
import {encodeManifestToProto} from './manifest2proto.js';

const opts = minimist(process.argv.slice(2), {
  string: ['outdir', 'outfile'],
  alias: {d: 'outdir', f: 'outfile'},
  default: {outdir: '.'}
});

if (opts.help || opts._.length === 0) {
  console.log(`
Usage
  $ tools/sigh manifest2proto [options] path/to/manifest.arcs

Description
  Serializes manifests to a protobuf file. 

Options
  --outfile, -f output filename; required
  --outdir, -d  output directory; defaults to '.'
  --help        usage info
`);
  process.exit(0);
}

if (!opts.outfile) {
  console.error(`Parameter --outfile is required.`);
  process.exit(1);
}

// TODO(alxr): Support proto generation from multiple manifests
if (opts._.length > 1) {
  console.error(`Only a single manifest is allowed`);
  process.exit(1);
}

if (opts._.some((file) => !file.endsWith('.arcs'))) {
  console.error(`Only Arcs manifests ('*.arcs') are allowed.`);
  process.exit(1);
}

async function main() {
  try {
    Runtime.init('../..');
    fs.mkdirSync(opts.outdir, {recursive: true});

    const buffer = await encodeManifestToProto(opts._[0]);

    const outPath = path.join(opts.outdir, opts.outfile);
    console.log(outPath);

    const outFile = fs.openSync(outPath, 'w');
    fs.writeSync(outFile, Buffer.from(buffer));
    fs.closeSync(outFile);
  } catch (e) {
    console.error(e);
    process.exit(1);
  }
}

void main();
