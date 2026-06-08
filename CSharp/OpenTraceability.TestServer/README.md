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

# Dataset IDs
The Test Server can isolate datasets by using the `X-Dataset-Id` header or optional 'datasetid` route paramter.

# Seeded Data
The test server seeds example data for the `default` dataset. This include a full EPCIS document with events and master data for all GDST example data.

