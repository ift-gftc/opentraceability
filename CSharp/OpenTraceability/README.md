# Open Traceability (C#)
This is an open-source library for handling traceability data in .NET using C#.

## Setup
Before you can use the library you need to make sure to call the Setup method.

If you are just using the core library, you should call:

`OpenTraceability.Setup.Initialize();`

However, if you are using an extension library such as:
- OpenTraceability.GDST
- OpenTraceability.MSC

Then you just need to call the setup method for that library such as:

- `OpenTraceability.GDST.Setup.Initialize();`
- `OpenTraceability.MSC.Setup.Initialize();`

And that will initialize everything you need.

## Models
We have C# models that represent the various data objects in EPCIS including events, documents, and master data.

### EPCIS Query Document / EPCIS Document
These two objects inherit from the same base class `EPCISBaseDocument` and represent the two types of documents in EPCIS. They are 
used to represent the XML and JSON-LD versions of the documents. The Events and Master Data are stored in the `EPCISBaseDocument.Events` 
and `EPCISBaseDocument.MasterData` properties respectively.

It's important to note that the `EPCISBaseDocument.EPCISVersion` property is used to determine the version of the document. This is important 
because when mapping from the models into the XML or JSON-LD formats, the version is used to determine which format to use. If you are
trying to map into EPCIS XML 2.0 and not EPCIS XML 1.2, then you need to make sure that the `EPCISBaseDocument.Version` property is set to `EPCISVersion.V2`.

#### EPCIS Document Header
The XML format for EPCIS 1.2 and 2.0 require a Standard Business Document Header (SBDH). This is represented by the `StandardBusinessDocumentHeader` class.
When converting from EPCIS JSON-LD into XML, you need to make sure to set this if check schema is enabled. Otherwise you will fail the schema validation.

> You can use `StandardBusinessDocumentHeader.DummyHeader` to get a dummy header that you can use to pass schema validation.

## Mapping
We support mapping between EPCIS 1.2 XML, EPCIS 2.0 XML, and EPCIS 2.0 JSON-LD. Our mappers are thread-safe and are accessible from the `OpenTraceability.Mappers.OpenTraceabilityMappers` static object.

### Reading EPCIS 1.2 XML

```csharp
// You can read an XML string representing an EPCIS 1.2 Query Document in XML format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr);
```

```csharp
// You can read an XML string representing an EPCIS 1.2 Document in XML format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(docXmlStr);
```

### Reading EPCIS 2.0 XML
This is done the same way as the 1.2 XML. It auto-detects the version and maps it to the correct object.

```csharp
// You can read an XML string representing an EPCIS 2.0 Query Document in XML format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr);
```

```csharp
// You can read an XML string representing an EPCIS 2.0 Document in XML format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(docXmlStr);
```

### Reading EPCIS 2.0 JSON-LD

```csharp
// You can read an XML string representing an EPCIS 2.0 Query Document in JSON-LD format.
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(queryDocXmlStr);
```

```csharp
// You can read an XML string representing an EPCIS 2.0 Document in JSON-LD format.
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(docXmlStr);
```

### Convert EPCIS 1.2 XML to EPCIS 2.0 XML
```csharp
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(queryDocXmlStr_1_2);

// make sure you set the version to 2.0
doc.EPCISVersion = EPCISVersion.V2;

string queryDocXmlStr_2_0 = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(doc);
```

## Querying for Data
We support querying traceability and master data using a very specific flavor of communication protocols from GS1.

- We support querying for event data using the `GET - /events` endpoint on the EPCIS 2.0 Query Interface.
- We support resolving master data from a GS1 Digital Link Resolver using the GS1 Web Vocab JSON-LD format.

### Querying for Event Data
You can query for event data by either passing in specifc parameters or by calling our trace-back method.

```csharp
EPC epc = new EPC("urn:epc:id:sgtin:0614141.107346.2019");
EPCISQueryParameters parameters = new EPCISQueryParameters(epc);
var results = await client.QueryEvents(blob_id, parameters);
```

```csharp

```