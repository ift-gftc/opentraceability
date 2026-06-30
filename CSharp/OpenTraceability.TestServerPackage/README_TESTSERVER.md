# Open Traceability — Test Server Package

Shared core of the OpenTraceability test server, plus tooling for using it from tests and other
services:

- **Core services + SQLite store** — ingestion, EPCIS query, master data, digital link resolution,
  and dataset persistence, shared by the real ASP.NET test server and the in-process WireMock host
  so behavior never diverges.
- **`TestServerClient`** — a typed HTTP client aligned 1:1 with the test server's controllers.
- **`WireMockTraceabilityServer`** — an in-process, in-memory server for fast unit tests.

## Datasets and modules

Data is isolated into datasets, and every dataset is a persisted record carrying its own GDST
module set (Seafood, Wildcaught, Aquaculture; Core is always implied and Wildcaught/Aquaculture
imply Seafood). A dataset's modules drive module minification of every EPCIS / master data
response served from it, so one server can serve core-only data and full GDST data side by side.
Datasets are selected by the `/{datasetId}/...` route prefix — the same mechanism a GDST capability
tool follows when handed a dataset-scoped digital-link root — and **unknown datasets return 404**.

## Typed client

```csharp
using var client = new TestServerClient("https://my-test-server", apiKey: "test");

// create a dataset with the modules it should serve
await client.UpsertDatasetAsync("my-test", new UpsertDatasetRequest
{
    Modules = new() { "Seafood", "Wildcaught" }
});

// dataset-scoped operations use the /{datasetId}/... path prefix
var ds = client.ForDataset("my-test");
await ds.PostEpcisDocumentAsync(epcisDocument);
var results = await ds.QueryEventsAsync(new EPCISQueryParameters());
var links = await ds.GetProductLinksAsync("09506000134376");

// hand this to the GDST capability tool — every request it makes stays inside the dataset
string resolverRoot = ds.DigitalLinkUrl;   // https://my-test-server/my-test/digitallink/

// run the full capability test (modules come from the dataset record)
var report = await client.RunCapabilityTestAsync(new CapabilityTestRequest
{
    ToolUrl = "https://capability-tool",
    ToolApiKey = "...",
    SolutionName = "MySolution",
    Pgln = "urn:gdst:example.org:party:me",
    DatasetId = "my-test",
    ClearDatasetBeforeRun = true
});
```

## In-process WireMock server

```csharp
using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
{
    Modules = new() { GdstModule.Wildcaught },          // modules of the primary dataset
    SeedEpcisDocuments = new() { epcisJson },
    Datasets = new()                                     // additional datasets, each with its own tier
    {
        new WireMockDataset { DatasetId = "core-tier", Modules = new() },
        new WireMockDataset { DatasetId = "full-tier", Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture } }
    }
});

// GET {server.Url}/epcis/events              -> primary dataset
// GET {server.Url}/core-tier/epcis/events    -> core-only minified
// GET {server.Url}/full-tier/epcis/events    -> full GDST data
```

The WireMock host serves GET routes only (digital link, EPCIS query, master data); ingestion,
traceback, capability tests and dataset management are only available on the real server. Datasets
are declared via the config.

## Seed manifests

The real server seeds datasets from `SeedData/` folders. A folder may carry a `dataset.json`
manifest declaring its modules, description, and shared data files:

```json
{ "modules": ["Seafood", "Wildcaught"], "description": "...", "files": ["_shared/gdst-testdata01.json"] }
```

Folders starting with `_` hold shared assets and are not datasets. Four canonical GDST datasets
ship with the package: `gdst-core`, `gdst-seafood`, `gdst-wildcatch`, and `gdst-full`.
