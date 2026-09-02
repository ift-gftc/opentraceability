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

The container runs as the non-root `app` user (uid 1654). `/app` and `/data` are made writable for
it in the image, because the default connection string (`Data Source=epcis.db`) makes the server
create its SQLite file in the working directory on first start. If you have a `testserver-data`
volume from an earlier root-owned image, either discard it
(`docker compose -f ./CSharp/docker-compose.yml down -v`) or re-own it
(`docker run --rm -v testserver-data:/data busybox chown -R 1654:1654 /data`).

# Docker build context
The build context is the **repository root**, not this folder: `OpenTraceability.csproj` embeds four
schema documents through `..\..\docs\` links, so `docs/` has to be reachable alongside `CSharp/`.

That root carries roughly 1.1 GB the image never needs — `.git/`, `Java/`, `.vs/`, and the `bin`/`obj`
of all eight C# projects. [`Dockerfile.dockerignore`](Dockerfile.dockerignore) trims it to ~1.5 MB.
It is a **strict allowlist**: it denies `**`, then re-includes only the four projects in this
project's `ProjectReference` closure plus the four embedded schema documents.

- **Adding a `ProjectReference`, or a new `docs/` embedded resource?** Add the matching
  `!CSharp/<project>/**` or `!docs/<path>` line. `DockerIgnoreClosureTests` (a plain unit test, no
  daemon needed) fails in milliseconds and names what is missing; without the fix the container
  build dies several minutes in with an opaque MSB3202.
- **The filename is deliberate.** BuildKit reads `<dockerfile-name>.dockerignore` next to the
  Dockerfile before falling back to the context root, which scopes these rules to this one image —
  DiagnosticsTool builds from the same root context and still needs the whole tree.
- **`appsettings.Development.json` is excluded from the context**, so it is not published into the
  image. It sets `BaseURL` to `https://localhost:7213`; when it was baked in, a container run with
  `ASPNETCORE_ENVIRONMENT=Development` emitted digital links pointing at that address. The server
  now falls back to request-derived URLs. Local `dotnet run` / F5 are unaffected.

`DockerBuildTests` asserts both halves of this automatically — that the build context holds only the
closure, and that `/app` holds only runtime assets. To additionally cross-check a built image
against your working tree's git-ignored files (worthwhile on a dev box with a fully built tree, and
vacuous on a clean CI checkout):

```powershell
docker build --target build -f ./CSharp/OpenTraceability.TestServer/Dockerfile -t testserver-buildstage:probe .
$ignored = git status --porcelain --ignored=matching |
    Where-Object { $_ -like '!!*' } |
    ForEach-Object { ($_ -replace '^!!\s+','').Trim('"').TrimEnd('/') } |
    Where-Object { $_ -like 'CSharp/*' } |
    ForEach-Object { '/src/' + ($_ -replace '^CSharp/','') }
$inImage = docker run --rm --entrypoint find testserver-buildstage:probe /src -mindepth 1
$leaked  = $inImage | Where-Object { $p = $_; ($ignored | Where-Object { $p -eq $_ -or $p.StartsWith($_ + '/') }).Count -gt 0 }
if ($leaked) { "LEAKED:"; $leaked } else { "clean: no git-ignored path reached /src" }
```

Matches under `obj/` are expected — the container's own `dotnet restore` creates them. Host leakage
is distinguishable by `Debug`, `net7.0` or `net9.0` path segments, since the container only builds
Release for net10.0/netstandard2.0.

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

