//const fs = require('fs');
//const { importWasm, importWasmSync } = require('import-wasm');

//const results = importWasmSync("../OpenTraceability/bin/Debug/net7.0/OpenTraceability.wasm", { env: {} });
//console.log(results);

import { WASI } from "wasi-js";
import fs from "fs";
import nodeBindings from "wasi-js/dist/bindings/node";

const wasi = new WASI({
    args: [],
    env: {},
    bindings: { ...nodeBindings, fs },
});

const source = await readFile("../OpenTraceability/bin/Debug/net7.0/OpenTraceability.wasm");
const typedArray = new Uint8Array(source);
const result = await WebAssembly.instantiate(typedArray, wasmOpts);
wasi.start(result.instance);