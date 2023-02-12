"use strict";
const fs = require("fs");
const { WASI } = require("wasi");
const wasi = new WASI();
const importObject = { wasi_snapshot_preview1: wasi.wasiImport };

(async () => {
    const wasm = await WebAssembly.compile(
        fs.readFileSync("../OpenTraceability/bin/Debug/net7.0/OpenTraceability.wasm")
    );
    const instance = await WebAssembly.instantiate(wasm, importObject);
    console.log(instance);
    wasi.start(instance);
})();