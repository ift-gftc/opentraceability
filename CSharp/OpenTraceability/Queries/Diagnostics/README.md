# Diagnostics System

The Diagnostics system provides comprehensive validation and debugging capabilities for traceability queries in OpenTraceability. It tracks requests, validates responses, and collects detailed information that can be used to troubleshoot issues with Digital Link resolvers, EPCIS Query Interfaces, and Master Data resolvers.

## Overview

The diagnostics system consists of several key components:

- **DiagnosticsReport**: The main container that tracks all diagnostics information across multiple requests
- **DiagnosticsRequest**: Represents individual requests made during traceability resolution  
- **Validation Rules**: Pluggable rules that validate different aspects of requests and responses
- **DiagnosticsValidationResult**: Results returned by validation rules

## Core Classes

### DiagnosticsReport

The `DiagnosticsReport` class is the main entry point for diagnostics tracking. It:

- Maintains a chronological list of requests made during traceability resolution
- Collects validation results from all executed rules
- Provides a `HasSchemaErrors` property to quickly identify schema validation issues
- Supports executing validation rules through `ExecuteRuleAsync<T>()`

Key properties:
- `Requests`: List of all requests made in chronological order
- `Validations`: Global validation results
- `CurrentRequest`: The most recent request being processed

### DiagnosticsRequest

Represents a single request made to a traceability service. Each request:

- Stores the request options used
- Maintains its own list of validation results
- Can execute validation rules specific to that request

### Validation Rules

All validation rules implement `IDiagnosticsRequestRule` and provide:

- A unique `Key` identifier
- An `ExecuteAsync()` method that returns validation results
- Specific validation logic for different scenarios

## Rule Categories

### Digital Link Rules

- **DigitalLinkHttpRequestRule**: Validates HTTP request headers and structure
- **DigitalLinkHttpResponseRule**: Validates HTTP response from Digital Link resolver
- **DigitalLinkJsonSchemaRule**: Validates JSON schema compliance
- **DigitalLinkResponseFoundRule**: Validates that a valid response was found

### EPCIS Rules

- **EPCISHttpRequestRule**: Validates EPCIS query request headers
- **EPCISHttpResponseRule**: Validates EPCIS query response
- **EPCISResponseSchemaRule**: Validates EPCIS response against schema
- **EPCISDuplicateEventIDsRule**: Checks for duplicate event IDs
- **EPCISMasterDataResolvedRule**: Validates master data resolution

### Master Data Rules

- **MasterDataHttpResponseRule**: Validates HTTP response from master data resolver
- **MasterDataJsonSchemaRule**: Validates master data JSON schema compliance
- **MasterDataValidResponseRule**: Validates that returned master data matches the query

## Validation Types

The system supports different types of validation results:

- **GeneralError**: General validation failures
- **HttpError**: HTTP-related issues (status codes, headers, etc.)
- **SchemaError**: JSON/XML schema validation failures
- **BusinessRuleError**: Business logic violations

Each validation result includes:
- **Level**: Log level (Error, Warning, Info, etc.)
- **Type**: The validation type from above
- **RuleKey**: Identifier of the rule that generated the result
- **Message**: Descriptive error message
- **InstanceLocation**: JSON Pointer to the element that caused the result, for
  schema errors. Empty for the document root, null when the result is not tied
  to a single element.
- **Value**: The value found at that location, when it is a scalar.

Schema validation reports one result per offending element, naming the element,
its value, and every reason it was rejected:

```text
/epcisBody/eventList/0/ilmd/gdst:broodstockSource — 'InvalidValue'
    enum: Value should match one of the values specified by the enum
```
Failures that did not affect the outcome are not reported: a failing `if`, which
is how a schema selects a branch by event type, and the losing side of an `anyOf`
whose other side matched.

## Usage in EPCISTraceabilityResolver

The diagnostics system is integrated into the traceability resolution process:

```csharp
// Create a new request entry
report?.NewRequest(options);

// Execute validation rules at appropriate points
await report?.ExecuteRuleAsync<DigitalLinkHttpRequestRule>(request.Headers);
await report?.ExecuteRuleAsync<DigitalLinkHttpResponseRule>(response.Headers);
await report?.ExecuteRuleAsync<DigitalLinkJsonSchemaRule>(json);
```

## Integration

To use diagnostics in your traceability queries:

1. Create a `DiagnosticsReport` instance
2. Pass it to traceability methods like `GetEPCISQueryInterfaceURL()`, `Traceback()`, or `QueryEvents()`
3. After execution, inspect the report for validation results and errors
4. Use `HasSchemaErrors` for quick error detection

## Example

```csharp
var report = new DiagnosticsReport();
var results = await EPCISTraceabilityResolver.Traceback(options, epc, client, null, report);

// Check for any schema errors
if (report.HasSchemaErrors)
{
    // Handle schema validation issues
}

// Review all validation results
foreach (var validation in report.Validations)
{
    Console.WriteLine($"{validation.Level}: {validation.Message}");
}
```

## Development Status

The diagnostics framework is currently under development. Many validation rules contain placeholder implementations (`throw new NotImplementedException()`) and will be implemented as the system matures.
