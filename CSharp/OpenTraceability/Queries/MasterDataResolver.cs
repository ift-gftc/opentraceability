using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries.Diagnostics;
using OpenTraceability.Queries.Diagnostics.Rules;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Mime;
using System.Threading.Tasks;

namespace OpenTraceability.Queries
{
    /// <summary>
    /// This class is used for resolving master data from a digital link resolver. It expects
    /// that links form the digital link resolver will return links that resolve to the GS1
    /// Web Vocab JSON-LD format.
    /// </summary>
    public class MasterDataResolver
    {
        public static async Task ResolveMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null)
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        await ResolveTradeitem(options, p.EPC.GTIN, doc, client, report);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc, client, report);

                foreach (var source in evt.SourceList)
                {
                    if (source.ParsedType == EventSourceType.Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty(options, pgln, doc, client, report: report);
                        }
                    }
                }

                foreach (var dest in evt.DestinationList)
                {
                    if (dest.ParsedType == EventDestinationType.Owner)
                    {
                        var pgln = new PGLN(dest.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty(options, pgln, doc, client, report: report);
                        }
                    }
                }
            }
        }

        public static async Task ResolveMasterData<TTradeitem, TLocation, TTradingParty>(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null) where TTradeitem : Tradeitem, new() where TLocation : Location, new() where TTradingParty : TradingParty, new()
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        await ResolveTradeitem<TTradeitem>(options, p.EPC.GTIN, doc, client, report);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc, client, report);

                foreach (var source in evt.SourceList)
                {
                    if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc, client, report);
                        }
                    }
                }

                foreach (var dest in evt.DestinationList)
                {
                    if (dest.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                    {
                        var pgln = new PGLN(dest.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc, client, report);
                        }
                    }
                }
            }
        }

        public static async Task ResolveTradeitem(DigitalLinkQueryOptions options, GTIN gtin, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null)
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Tradeitem)) ?? typeof(Tradeitem);
                    var ti = (await ResolveMasterDataItem(t, options, $"{gtin.ToDigitalLinkURL()}?linkType=gs1:masterData", client, false, gtin.ToString(), report)) as Tradeitem;
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation(DigitalLinkQueryOptions options, GLN gln, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null)
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Location)) ?? typeof(Location);
                    var l = (await ResolveMasterDataItem(t, options, $"{gln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, false, gln.ToString(), report)) as Location;
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty(DigitalLinkQueryOptions options, PGLN pgln, EPCISBaseDocument doc, HttpClient client, bool addGDSTExtensionHeader = false, DiagnosticsReport? report = null)
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(TradingParty)) ?? typeof(TradingParty);
                    var tp = (await ResolveMasterDataItem(t, options, $"{pgln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, addGDSTExtensionHeader, pgln.ToString(), report)) as TradingParty;
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task ResolveTradeitem<T>(DigitalLinkQueryOptions options, GTIN gtin, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null) where T : Tradeitem
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    var ti = await ResolverMasterDataItem<T>(options, $"{gtin.ToDigitalLinkURL()}?linkType=gs1:masterData", client, gtin, report);
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation<T>(DigitalLinkQueryOptions options, GLN gln, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null) where T : Location
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    var l = await ResolverMasterDataItem<T>(options, $"{gln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, gln, report);
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty<T>(DigitalLinkQueryOptions options, PGLN pgln, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null) where T : TradingParty
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    var tp = await ResolverMasterDataItem<T>(options, $"{pgln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, pgln, report);
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task<Tradeitem?> ResolveTradeitem(DigitalLinkQueryOptions options, GTIN gtin, HttpClient client, DiagnosticsReport? report = null)
        {
            if (gtin != null)
            {
                var ti = await ResolverMasterDataItem<Tradeitem>(options, $"{gtin.ToDigitalLinkURL()}?linkType=gs1:masterData", client, gtin, report);
                if (ti != null)
                {
                    return ti;
                }
            }
            return null;
        }

        public static async Task<Location?> ResolveLocation(DigitalLinkQueryOptions options, GLN gln, HttpClient client, DiagnosticsReport? report = null)
        {
            if (gln != null)
            {
                var l = await ResolverMasterDataItem<Location>(options, $"{gln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, gln, report);
                if (l != null)
                {
                    return l;
                }
            }
            return null;
        }

        public static async Task<TradingParty?> ResolveTradingParty(DigitalLinkQueryOptions options, PGLN pgln, HttpClient client, DiagnosticsReport? report = null)
        {
            if (pgln != null)
            {
                var tp = await ResolverMasterDataItem<TradingParty>(options, $"{pgln.ToDigitalLinkURL()}?linkType=gs1:masterData", client, pgln, report);
                if (tp != null)
                {
                    return tp;
                }
            }
            return null;
        }

        public static async Task<T?> ResolverMasterDataItem<T>(DigitalLinkQueryOptions options, string relativeURL, HttpClient client, object? originalIdentifier = null, DiagnosticsReport? report = null) where T : IVocabularyElement
        {
            var response = await ResolveMasterDataItem(typeof(T), options, relativeURL, client, false, originalIdentifier, report);
            if (response == null)
            {
                return default(T);
            }
            else
            {
                return (T)response;
            }
        }

        public static async Task<object?> ResolveMasterDataItem(Type type, DigitalLinkQueryOptions options, string relativeURL, HttpClient httpClient, bool addGDSTExtension = false, object? originalIdentifier = null, DiagnosticsReport? report = null)
        {
            // DIAGNOSTICS: Create a new request.
            report?.NewRequest("Resolve Master Data Link", options);

            try
            {
                if (options.URL == null)
                {
                    throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
                }

                HttpRequestMessage request = new HttpRequestMessage();
                request.RequestUri = new Uri(options.URL.ToString().TrimEnd('/') + "/" + relativeURL.TrimStart('/'));
                request.Method = HttpMethod.Get;

                // DIAGNOSTICS: Execute the rule to validate the HTTP response.
                if (report != null)
                {
                    report.CurrentRequest.HttpRequest = request;
                }

                var response = await httpClient.SendAsync(request);

                // DIAGNOSTICS: Execute the rule to validate the HTTP response.
                if (report?.CurrentRequest != null)
                {
                    report.CurrentRequest.HttpResponse = response;
                    await report.CurrentRequest.ExecuteRuleAsync<MasterDataHttpResponseRule>(response);
                }

                string responseStr = await response.Content.ReadAsStringAsync();
                if (report?.CurrentRequest != null)
                {
                    report.CurrentRequest.ResponseBody = responseStr;
                }

                if (response.IsSuccessStatusCode)
                {
                    // DIAGNOSTICS: Execute the rule to validate the JSON schema for digital links.
                    if (report?.CurrentRequest != null)
                    {
                        await report.CurrentRequest.ExecuteRuleAsync<DigitalLinkJsonSchemaRule>(responseStr);
                    }

                    var links = JsonConvert.DeserializeObject<List<DigitalLink>>(responseStr)?.Select(d => d as DigitalLink).ToList() ?? new List<DigitalLink>();
                    if (links.Count > 0)
                    {
                        foreach (var link in links)
                        {
                            // DIAGNOSTICS: Create a new request.
                            report?.NewRequest("Resolve Master Data", options);

                            try
                            {
                                request = new HttpRequestMessage();
                                request.RequestUri = new Uri(link.link);
                                request.Method = HttpMethod.Get;

                                if (report?.CurrentRequest != null)
                                {
                                    report.CurrentRequest.HttpRequest = request;
                                }

                                if (addGDSTExtension)
                                {
                                    request.Headers.AddGDSTExtensionHeader();
                                }

                                response = await httpClient.SendAsync(request);

                                // DIAGNOSTICS: Execute the rule to validate the master data HTTP response.
                                if (report?.CurrentRequest != null)
                                {
                                    report.CurrentRequest.HttpResponse = response;
                                    await report.CurrentRequest.ExecuteRuleAsync<MasterDataHttpResponseRule>(response);
                                }

                                if (response.IsSuccessStatusCode)
                                {
                                    var content = await response.Content.ReadAsStringAsync();
                                    var jObject = JsonConvert.DeserializeObject<object>(content);
                                    var json = JsonConvert.SerializeObject(jObject);

                                    if (json is null)
                                        throw new NullReferenceException("Error parsing the digital link response: JSON value is null");

                                    // DIAGNOSTICS: Execute the rule to validate the master data JSON schema.
                                    if (report?.CurrentRequest != null)
                                    {
                                        report.CurrentRequest.ResponseBody = content;
                                        await report.CurrentRequest.ExecuteRuleAsync<MasterDataJsonSchemaRule>(json);
                                    }

                                    var item = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(type, json);
                                    if (item != null)
                                    {
                                        if (item.ID == null)
                                        {
                                            throw new Exception($"While resolve a {type} through the GS1 Digital Link Resolver, the {type} returned " +
                                                $"had an empty or invalid Identifier. The link that was resolved was " + link + " and the results was " + json);
                                        }
                                        else
                                        {
                                            // DIAGNOSTICS: Execute the rule to validate that the response matches the query.
                                            if (report?.CurrentRequest != null && originalIdentifier != null)
                                            {
                                                await report.CurrentRequest.ExecuteRuleAsync<MasterDataValidResponseRule>(originalIdentifier, item);
                                            }

                                            return item;
                                        }
                                    }
                                }
                                else
                                {
                                    var content = await response.Content.ReadAsStringAsync();
                                    if (report?.CurrentRequest != null)
                                    {
                                        report.CurrentRequest.ResponseBody = content;
                                    }
                                }
                            }
                            catch (Exception ex)
                            {
                                Console.WriteLine(ex);
                            }
                        }
                    }
                }
                else
                {
                    // DO NOTHING
                }

                return null;
            }
            catch (Exception ex)
            {
                if (report != null && report.CurrentRequest != null)
                {
                    report.CurrentRequest.AddException(ex);
                }

                return null;
            }            
        }
    }
}