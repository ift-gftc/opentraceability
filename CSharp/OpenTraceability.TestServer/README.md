# Summary
The Test Server is a simple web server that can be used to test the OpenTraceability library.
It provides endpoints for testing various features of the library, such as parsing and validating EPCIS documents.

# Warning
The Test Server is not intended for production use and should only be used for testing and demonstration purposes.

# Building and Running
To build and run the Test Server, follow these steps:

Execute from the root of the OpenTraceability repository
**Build**
```bash
docker build -f ./CSharp/OpenTraceability.TestServer/Dockerfile -t opentraceability-testserver .
```

**Run**
```bash
docker run --name opentraceability-testserver -e ASPNETCORE_ENVIRONMENT=Development -p 5000:8080 opentraceability-testserver
```

# Datasets and Modules
The Test Server isolates data into datasets, selected by the leading `/{datasetId}/` route segment
(e.g. `GET /gdst-wildcatch/epcis/events`) or, on the bare routes, the `X-Dataset-Id` header
(falling back to `default`).

Every dataset is a persisted record carrying its own GDST module set (Seafood, Wildcaught,
Aquaculture — Core is always implied, and Wildcaught/Aquaculture imply Seafood). The dataset's
modules drive module minification of every EPCIS and master data response served from it, so one
deployed server can serve core-only data on one dataset and full seafood/wildcaught/aquaculture
data on another. **A dataset must exist before it can be read or written — requests for unknown
datasets return 404.**

Datasets are managed via the `/datasets` API:

| Endpoint | Description |
|---|---|
| `GET /datasets` | List all datasets and their modules |
| `GET /datasets/{id}` | Get one dataset |
| `POST /datasets` | Create (409 if it exists) — body: `{ "datasetId", "modules", "description" }` |
| `PUT /datasets/{id}` | Idempotent create-or-update — body: `{ "modules", "description" }` |
| `DELETE /datasets/{id}?purgeData=true` | Delete the record (optionally its data) |
| `POST /datasets/{id}/clear` | Purge the dataset's data, keep the record |

Reserved route roots (`epcis`, `masterdata`, `digitallink`, `datasets`, `traceback`,
`capability-test`, `health`, `swagger`) cannot be used as dataset ids.

The `Modules` list in `appsettings.json` only defines the modules of the `default` dataset, which
is bootstrapped at startup when absent (so edits made via the API survive restarts).

# Capability Tests
`POST /capability-test/run` (or `POST /{datasetId}/capability-test/run`) runs the full GDST 2.0
capability test as a solution-provider client against a capability tool. The test is tied to a
dataset: the dataset's persisted modules are sent to the tool, the resolver URL handed to the tool
is `{BaseURL}/{datasetId}/digitallink/` so every request the tool makes is served (and minified)
from that dataset, and the tool's generated data is stored into it. Optional
`clearDatasetBeforeRun: true` purges the dataset first so repeated runs don't accumulate stale EPCs.

# Seeded Data
Seed datasets live under `SeedData/` — each folder is a dataset, optionally carrying a
`dataset.json` manifest:

```json
{ "modules": ["Seafood", "Wildcaught"], "description": "...", "files": ["_shared/gdst-testdata01.json"] }
```

`modules` declares the dataset's module set, and `files` references shared data files (relative to
the SeedData root; folders starting with `_` are shared assets, not datasets). The server ships
four canonical GDST datasets, all backed by the same full-fidelity document and differing only in
their served modules:

| Dataset | Modules served |
|---|---|
| `gdst-core` | Core only |
| `gdst-seafood` | Seafood |
| `gdst-wildcatch` | Seafood + Wildcaught |
| `gdst-full` | Seafood + Wildcaught + Aquaculture |

Plus `default` (modules from `appsettings.json`) and `beef-leather-example` (core-only, non-seafood).

# Upgrading an existing deployment
The `Datasets` table is added to an existing `epcis.db` automatically at startup. Data ingested
under ad-hoc dataset ids before the upgrade will 404 until the dataset is registered via
`PUT /datasets/{id}` (the `default` dataset is bootstrapped automatically). Note that the Java
`EPCISTestServerClient` port does not yet create datasets and will receive 404s against an
upgraded server.

