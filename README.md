# Open Traceability
A repository for storing code for the open-source traceability libraries.

## Funded By
These open-source libraries would not be possible without great organizations who took the initiative to fund this project. We want to give a special thanks to:

- Gordon and Betty Moore Foundation
- Walton Family Foundation

## Current Supported Programming Languages / Frameworks
We currently support the following:
- C#
    - .NET 7 (supported)
    - .NET Standard 2.0 (coming soon)

- Java (actively being translated and coming soon)
- TypeScript (coming soon)

*If you want us to support your language, please reach out to us so we can put on our to-do list.*

# Open Traceability (C#)
This is an open-source library for handling traceability data in .NET using C#.

## Setup
Before you can use the library you need to make sure to call the Setup method.

If you are just using the core library, you should call:

`OpenTraceability.Setup.Initialize();`

However, if you are using an extension library such as:
- OpenTraceability.GDST

Then you just need to call the setup method for that library such as:

- `OpenTraceability.GDST.Setup.Initialize();`

And that will initialize everything you need.

## Models
We have C# models that represent the various data objects in EPCIS including events, documents, and master data. Feel free 
to explore them.

> This library was designed so that it could easily be extended to support many new CTEs/KDEs. It was also designed so that
even without an extension library, the core library can still receive CTEs/KDEs from unknown extensions and namespaces and
serialize/deserialize the data without losing any information.

> More information on extensions can be found in the documentation later on. (coming soon)

### EPCIS Query Document / EPCIS Document
These two objects inherit from the same base class `EPCISBaseDocument` and represent the two types of documents in EPCIS. They are 
used to represent the XML and JSON-LD versions of the documents. The Events and Master Data are stored in the `EPCISBaseDocument.Events` 
and `EPCISBaseDocument.MasterData` properties respectively.

It's important to note that the `EPCISBaseDocument.EPCISVersion` property is used to determine the version of the document. This is important 
because when mapping from the models into the XML or JSON-LD formats, the version is used to determine which format to use. If you are
trying to map into EPCIS XML 2.0 and not EPCIS XML 1.2, then you need to make sure that the `EPCISBaseDocument.Version` property is set to `EPCISVersion.V2`.

#### EPCIS Document Header
The XML format for EPCIS 1.2 and 2.0 require a Standard Business Document Header (SBDH). This is represented by the `StandardBusinessDocumentHeader` class.
When converting from EPCIS JSON-LD into XML, you need to make sure to set this if the check schema is enabled. Otherwise, you will fail the schema validation.

> You can use `StandardBusinessDocumentHeader.DummyHeader` to get a dummy header that you can use to pass schema validation for the XML formats.

## Mapping
We support mapping between EPCIS 1.2 XML, EPCIS 2.0 XML, and EPCIS 2.0 JSON-LD. Our mappers are thread-safe and are accessible from the `OpenTraceability.Mappers.OpenTraceabilityMappers` static object.

### Reading EPCIS 1.2 XML
In order to read an EPCIS Query Document from an XML string in the EPCIS 1.2 xml format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 1.2 Query Document in XML format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr);
```

In order to read an EPCIS Document from an XML string in the EPCIS 1.2 xml format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 1.2 Document in XML format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(docXmlStr);
```

### Reading EPCIS 2.0 XML
This is done the same way as the 1.2 XML above. It auto-detects the version and maps it to the correct object.

In order to read an EPCIS Query Document from an XML string in the EPCIS 2.0 xml format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 2.0 Query Document in XML format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr);
```

In order to read an EPCIS Document from an XML string in the EPCIS 2.0 xml format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 2.0 Document in XML format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(docXmlStr);
```

### Reading EPCIS 2.0 JSON-LD
In order to read an EPCIS Query Document from a JSON string in the EPCIS 2.0 JSON-LD format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 2.0 Query Document in JSON-LD format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(queryDocXmlStr);
```

In order to read an EPCIS Document from a JSON string in the EPCIS 2.0 JSON-LD format you can do the following:
```csharp
// You can read an XML string representing an EPCIS 2.0 Document in JSON-LD format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(docXmlStr);
```

### Convert EPCIS 1.2 XML to EPCIS 2.0 XML
This example is for reading an EPCIS Query Document, but it would work the same with EPCIS Document as well.
```csharp
// read the EPCIS 1.2 XML string 
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr_1_2);

// set the EPCIS version
doc.EPCISVersion = EPCISVersion.V2;

// write it back out now
string queryDocXmlStr_2_0 = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(doc);
```

### Convert EPCIS 1.2 XML to EPCIS 2.0 JSON-LD
This example is for reading an EPCIS Query Document, but it would work the same with EPCIS Document as well.
```csharp
// read the EPCIS 1.2 XML string 
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr_1_2);

// set the EPCIS version
doc.EPCISVersion = EPCISVersion.V2;

// write it back out now
string queryDocJsonStr_2_0 = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);
```

## Querying for Data
We support querying traceability and master data using a very specific flavor of communication protocols from GS1.

- We support querying for event data using the `GET - /events` endpoint on the EPCIS 2.0 Query Interface.
- We support resolving master data from a GS1 Digital Link Resolver using the GS1 Web Vocab JSON-LD format.

### EPCIS Query Interface
The first interface that can be queried is for events from an EPCIS 2.0 Query Interface at the `GET - /events` endpoint. 
This is done using the `EPCISTraceabilityResolver` class.

In order to use this, you need:
- `EPCISQueryInterfaceOptions`
	- URL
	- Version
	- Format

- `EPCISQueryParameters`
	- Here you can define one or more parameters for filtering the desired results.

```csharp
using HttpClient = new HttpClient();

EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
{
    URL = new Uri(url),
    Format = _format,
    Version = _version,
    EnableStackTrace = true
};

EPC epc = new EPC("urn:epc:id:sgtin:0614141.107346.2019");
EPCISQueryParameters parameters = new EPCISQueryParameters(epc);

return await EPCISTraceabilityResolver.QueryEvents(options, parameters, client);
```

You can also use the trace-back feature to automatically trace-back the EPC.

```csharp
using HttpClient = new HttpClient();

EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions()
{
    URL = new Uri(url),
    Format = _format,
    Version = _version,
    EnableStackTrace = true
};

EPC epc = new EPC("urn:epc:id:sgtin:0614141.107346.2019");

return await EPCISTraceabilityResolver.Traceback(options, epc, client);
```

### GS1 Digital Link Resolver
You can also resolve master data from a GS1 Digital Link resolver with the `MasterDataResolver` class. This class
takes in a `DigitalLinkQueryOptions` object that contains the URL of the resolver and also an EPCISBaseDocument
and will search the EPCISBaseDocument for any master data that is not in the document but referenced in the events,
and try and resolve it and add it to the document.

```csharp	
DigitalLinkQueryOptions options = new DigitalLinkQueryOptions()
{
	URL = new Uri(url),
	EnableStackTrace = true
};

await MasterDataResolver.ResolveMasterData(options, doc, client);
```

## Finally
You can always look in our unit tests for more examples of how to use the library.

