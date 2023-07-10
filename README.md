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

## Accessing the Packages
You can access the packges for the various languages like so:
- **C#** - Nuget Feed
  - https://www.nuget.org/packages/OpenTraceability
  - https://www.nuget.org/packages/OpenTraceability.GDST
 
- **Java**
  - We are waiting for approval to post the java libraries to the Maven network, but for now you can download the source code, build and use the JAR libraries this way.
 
## Not happy?
If you find any issues, please contact me directly at pheggelund@ift.org and we will fix the problem the best we can.

## Help?
This ReadMe file should contain documentation on how to use the libraries in C# and Java, please just keep scrolling. If you find it lacking, please let us know.

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

# OpenTraceability for Java
These are libraries that can be used for working with EPCIS data in Java. These libraries were built using JDK11 and are written in pure Java. 

The follow dependencies were used:
- org.json
- org.apache.karaf.http.core
- com.network.json-schema-validator
- gson
- junit
- org.junit.jupiter
- com.squareup.okhttp

> This documentation assumes you are familiar with EPCIS and GS1 Digital Link. Being familiar with the GDST 1.2 communication protocol is a plus as well.

> This documentation is constantly being updated. If you cannot find information that you are looking for, please post an issue on GitHub.

## EPCIS
This library can work with both EPCIS 1.2 (XML), and EPCIS 2.0 (XML/JSON-LD) and only partially implements the communication protocol from EPCIS.

## Extensibility
These libraries were designed with EXTENSIBILITY at heart. It was designed so that adding new CTEs / KDEs would be as simple as defining models that extend the existing ones. 

Current Extensions
- GDST

## Models
This library provides model support for working with EPCIS data. The models can be found in the `opentraceability.models` and `opentraceability.interfaces` packages.

The core document models are:
- EPCISDocument
- EPCISQueryDocument

These represent documents that act as a collection of events and master data.

Next we have the event models which are:
- ObjectEvent
- TransformationEvent
- TransactionEvent
- AggregationEvent
- AssociationEvent

Which represent the core events from EPCIS.

Finally, we have the master data models which are:
- IVocabularyEvent (abstract)
- VocabularyEvent (unknown)
- TradeItem 
- Location
- TradingParty

## Initialization
It is important that you always initialize the libraries prior to using them or you could have unexpected behavior.

In order to initialize the core libraries you need to call:
```agsl
opentraceability.Setup.Initialize();
```

> This call uses locking and also will not execute more than once. If you call it a second time, it will do nothing.

## Mapping
We provide mappings for:
- Mapping an EPCISDocument/EPCISQueryDocument to/from XML for both EPCIS 1.2 and EPCIS 2.0.
- Mapping an EPCISDocument/EPCISQueryDocument to/from JSON-LD for EPCIS 2.0.
- Mapping a master data object (IVocabularyElement) to/from GS1 Web Vocab JSON-LD.

All of the mappers are accessed via a static object at `opentraceability.mappers.OpenTraceabilityMappers`.

### Mapping EPCIS XML 1.2/2.0
You can map an XML string to/from an EPCIS Query Document like so:
```
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlStr, true);
String xmlStrAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(doc);
```

You can map an XML string to/from an EPCIS Document like so:
```
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.map(xmlStr, true);
String xmlStrAfter = OpenTraceabilityMappers.EPCISDocument.XML.map(doc);
```

> The version of the XML is controlled via the *version* property on the EPCISDocument/EPCISQueryDocument. When mapping from XML, it is detected by the namespace that is used.

### Mapping EPCIS JSON-LD 2.0
You can map an JSON-LD string to/from an EPCIS Document like so:
```
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(jsonStr, true);
String jsonStrAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc);
```

You can map a JSON-LD string to/from an EPCIS Query Document like so:
```
EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(jsonStr, true);
String jsonStrAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(doc);
```

### Mapping Master Data to/from GS1 Web Vocab JSON-LD
Below is how you would map a Trade Item in the GS1 Web Vocab JSON-LD format into a Trade Item object and back into the JSON-LD.
```agsl
// map into a trade item
TradeItem tradeitem = (TradeItem)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(TradeItem.class, jsonStr);

// map back into json
String jsonStrAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tradeitem);
```

Below is how you would map a Location in the GS1 Web Vocab JSON-LD format into a Location object and back into the JSON-LD.
```agsl
// map into a location
Location loc = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(Location.class, jsonStr);

// map back into json
String jsonStrAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(loc);
```

Below is how you would map a Trading Party in the GS1 Web Vocab JSON-LD format into a Trading Party object and back into the JSON-LD.
```agsl
// map into a trading party
TradingParty tradingParty = (TradingParty)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(TradingParty.class, jsonStr);

// map back into json
String jsonStrAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tradingParty);
```

## Querying for Data
Our libraries also support the ability to query for data using our clients. The communication protocol is based on the communication protocol from GDST that utilizes the EPCIS 2.0 Query Interface and GS1 Digital Link to query for events and then resolve master data.

### EPCIS Query Interface
Our libraries only support querying the EPCIS Query Interface **GET - /events** end point which allows you to submit filters via query paramters in the URL.

You can query for events using the `EPCISTraceabilityResolver` class which is static. This class will allow you to perform two functions:
- Query for Events
- Traceback

The query for events will only perform the query you specify, where the traceback will attempt to traceback the history of the product through it's source-products.

#### Querying Events
They `EPCISTraceabilityResolver.queryEvents` will only execute a query using the specified paramters.

> If you are not familiar with the parameters, please read about them in the EPCIS 2.0 standard.

```agsl
// load our EPCIS document
String data = ReadTestData(filename);
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data);
String blob_id = client.postEPCISDocument(doc);

EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions();
options.version = EPCISVersion.V2;
options.format = EPCISDataFormat.JSON;
options.url = URI.create("<enter EPCIS query interface endpoint here...>");
options.enableStackTrace = true; // this can help with debugging...

OkHttpClient client = new OkHttpClient();

// for each product in the document, query for all events relating to that product
for (IEvent e : doc.events) {
    for (EventProduct p : e.getProducts()) {
        
        // shortcut for creating query paramters with the EPC set to either MATCH_anyEPC or MATCH_anyEPCClass
        EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
        
        // query for all events related to this EPC
        EPCISQueryResults results = EPCISTraceabilityResolver.queryEvents(options, parameters, client);
        
        // we can access our results with results.Document. This is null in the event of an error.
        EPCISQueryDocument resultsDoc = results.Document;
        
        // we can access errors with results.Errors
        Boolean hasErrors = results.Errors.size() > 0;
    }
}
```


#### Tracebacks
Traceback is a common querying pattern in traceability data querying. This documentation will not explain the concept, however the high level is that you will query for all events relating to Product A, then scan for any inputs/children in the returned traceability data and repeat until no more unknown inputs/children remain.

This can be accessed using `EPCISTraceabilityResolver.traceback` method.

```agsl
// load our EPCIS document
String data = ReadTestData(filename);
EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data);
String blob_id = client.postEPCISDocument(doc);

EPCISQueryInterfaceOptions options = new EPCISQueryInterfaceOptions();
options.version = EPCISVersion.V2;
options.format = EPCISDataFormat.JSON;
options.url = URI.create("<enter EPCIS query interface endpoint here...>");
options.enableStackTrace = true; // this can help with debugging...

OkHttpClient client = new OkHttpClient();

// for each product in the document, query for all events relating to that product
for (IEvent e : doc.events) {
    for (EventProduct p : e.getProducts()) {
        
        // do a traceback on a specific EPC. on top of specifying the EPC, you can also specify additional EPCIS query
        // parameters, however in this case, we do not.
        EPCISQueryResults results = EPCISTraceabilityResolver.traceback(options, p.EPC, client, null);

        // we can access our results with results.Document. This is null in the event of an error.
        EPCISQueryDocument resultsDoc = results.Document;

        // we can access errors with results.Errors
        Boolean hasErrors = results.Errors.size() > 0;
    }
}
```

# GDST Extension
The OpenTraceability libraries have been extended to support the CTE/KDE matrix from GDST. This includes the following model extensions:

> These examples are for Java, but will apply for the other libraries as well.

- Events
  - GDST Fishing Event
  - GDST Transshipment Event
  - GDST Landing Event
  - GDST Hatching Event
  - GDST Feed Mill Event (Object - ADD / Transformation)
  - GDST Farm Harvest Event
  - GDST Shipping Event
  - GDST Receiving Event
  - GDST Processing Event
- ILMD
  - The base `EventILMD` was extended as `GDSTILMD` to support ILMD extensions in GDST.
- KDEs
  - The KDE `VesselCatchInformation` was created to support this KDE for fishing events.
- Master Data
  - The `GDSTLocation` extends the base `Location` and supports additional master data KDEs defined in GDST.
- Querying
  - The `GDSTMasterDataResolver` was added to support resolving master data for the `information provider` and `product owner` KDEs on each event.

In order to use the extension you need to call:

```agsl
// this will also call the core initlaization if you did not already
opentraceability.gdst.Setup.Initialize();
```

After this, as you deserialize EPCIS data into EPCIS Document and EPCIS Query Documents, you will find the objects in the `events` field are now instances of the GDST event models providing they match the event profile specified by the GDST standard.

For the master data, you will now receive back locations as an instance of `GDSTLocation` instead of `Location` even if you request `Location` in the mapper like:

```agsl
// map into a location
Location loc = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(Location.class, jsonStr);
```

If you look in the source code at `opentraceability.gdst.Setup.Initialize()` you will find where we define the profiles.
```agsl
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFishingEvent.class, EventType.ObjectEvent, "urn:gdst:bizStep:fishingEvent", EventAction.ADD));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTTransshipmentEvent.class, EventType.ObjectEvent, "urn:gdst:bizStep:transshipment", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTLandingEvent.class, EventType.ObjectEvent, "urn:gdst:bizstep:landing", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTHatchingEvent.class, EventType.ObjectEvent, "urn:gdst:bizstep:hatching", EventAction.ADD));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFarmHarvestEvent.class, EventType.TransformationEvent, "urn:gdst:bizstep:farmharvest"));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTProcessingEvent.class, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning"));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTProcessingEvent.class, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning"));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTReceiveEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:receiving", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTReceiveEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-receiving", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTShippingEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:shipping", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTShippingEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-shipping", EventAction.OBSERVE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTAggregationEvent.class, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:packing", EventAction.ADD));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTAggregationEvent.class, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-packing", EventAction.ADD));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTDisaggregationEvent.class, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:unpacking", EventAction.DELETE));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTDisaggregationEvent.class, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-unpacking", EventAction.DELETE));

// The feedmill event is special in the sense that it requires a KDE profile to detect.
// We know it is a feedmill event when it has the proteinSource KDE in the ILMD.
List<OpenTraceabilityEventKDEProfile> feedmillKDEProfile = new ArrayList<>();
feedmillKDEProfile.add(new OpenTraceabilityEventKDEProfile("extension/ilmd/" + (Constants.GDST_XNAMESPACE + "proteinSource"), "ilmd/" + (Constants.GDST_XNAMESPACE + "proteinSource"), "ilmd.gdst:proteinSource"));

opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent.class, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning", feedmillKDEProfile));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent.class, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", feedmillKDEProfile));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:commissioning", EventAction.ADD, feedmillKDEProfile));
opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", EventAction.ADD, feedmillKDEProfile));

opentraceability.Setup.registerMasterDataType(GDSTLocation.class, Location.class);
```
