using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Json;
using System.Text;
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
        public static async Task ResolveMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client)
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        await ResolveTradeitem(options, p.EPC.GTIN, doc, client);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc, client);

                foreach (var source in evt.SourceList)
                {
                    if (source.ParsedType == EventSourceType.Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty(options, pgln, doc, client);
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
                            await ResolveTradingParty(options, pgln, doc, client);
                        }
                    }
                }
            }
        }

        public static async Task ResolveMasterData<TTradeitem, TLocation, TTradingParty>(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client) where TTradeitem : Tradeitem, new() where TLocation : Location, new() where TTradingParty : TradingParty, new()
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        await ResolveTradeitem<TTradeitem>(options, p.EPC.GTIN, doc, client);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc, client);

                foreach (var source in evt.SourceList)
                {
                    if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc, client);
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
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc, client);
                        }
                    }
                }
            }
        }

        public static async Task ResolveTradeitem(DigitalLinkQueryOptions options, GTIN? gtin, EPCISBaseDocument doc, HttpClient client)
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Tradeitem)) ?? typeof(Tradeitem);
                    var ti = (await ResolveMasterDataItem(t, options, $"/01/{gtin}?linkType=gs1:masterData", client)) as Tradeitem;
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation(DigitalLinkQueryOptions options, GLN? gln, EPCISBaseDocument doc, HttpClient client)
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Location)) ?? typeof(Location);
                    var l = (await ResolveMasterDataItem(t, options, $"/414/{gln}?linkType=gs1:masterData", client)) as Location;
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty(DigitalLinkQueryOptions options, PGLN? pgln, EPCISBaseDocument doc, HttpClient client)
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(TradingParty)) ?? typeof(TradingParty);
                    var tp = (await ResolveMasterDataItem(t, options, $"/417/{pgln}?linkType=gs1:masterData", client)) as TradingParty;
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task ResolveTradeitem<T>(DigitalLinkQueryOptions options, GTIN? gtin, EPCISBaseDocument doc, HttpClient client) where T : Tradeitem
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    var ti = await ResolverMasterDataItem<T>(options, $"/01/{gtin}?linkType=gs1:masterData", client);
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation<T>(DigitalLinkQueryOptions options, GLN? gln, EPCISBaseDocument doc, HttpClient client) where T : Location
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    var l = await ResolverMasterDataItem<T>(options, $"/414/{gln}?linkType=gs1:masterData", client);
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty<T>(DigitalLinkQueryOptions options, PGLN? pgln, EPCISBaseDocument doc, HttpClient client) where T : TradingParty
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    var tp = await ResolverMasterDataItem<T>(options, $"/417/{pgln}?linkType=gs1:masterData", client);
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task<Tradeitem?> ResolveTradeitem(DigitalLinkQueryOptions options, GTIN? gtin, HttpClient client)
        {
            if (gtin != null)
            {
                var ti = await ResolverMasterDataItem<Tradeitem>(options, $"/01/{gtin}?linkType=gs1:masterData", client);
                if (ti != null)
                {
                    return ti;
                }
            }
            return null;
        }

        public static async Task<Location?> ResolveLocation(DigitalLinkQueryOptions options, GLN? gln, HttpClient client)
        {
            if (gln != null)
            {
                var l = await ResolverMasterDataItem<Location>(options, $"/414/{gln}?linkType=gs1:masterData", client);
                if (l != null)
                {
                    return l;
                }
            }
            return null;
        }

        public static async Task<TradingParty?> ResolveTradingParty(DigitalLinkQueryOptions options, PGLN? pgln, HttpClient client)
        {
            if (pgln != null)
            {
                var tp = await ResolverMasterDataItem<TradingParty>(options, $"/417/{pgln}?linkType=gs1:masterData", client);
                if (tp != null)
                {
                    return tp;
                }
            }
            return null;
        }

        public static async Task<T?> ResolverMasterDataItem<T>(DigitalLinkQueryOptions options, string relativeURL, HttpClient client) where T : IVocabularyElement
        {
            var response = await ResolveMasterDataItem(typeof(T), options, relativeURL, client);
            if (response == null)
            {
                return default(T);
            }
            else
            {
                return (T)response;
            }
        }

        public static async Task<object?> ResolveMasterDataItem(Type type, DigitalLinkQueryOptions options, string relativeURL, HttpClient httpClient)
        {
            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
            }

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL.ToString().TrimEnd('/') + "/" + relativeURL.TrimStart('/'));
            request.Method = HttpMethod.Get;

            var response = await httpClient.SendAsync(request);

            if (response.IsSuccessStatusCode)
            {
                string responseStr = await response.Content.ReadAsStringAsync();
                var links = JsonConvert.DeserializeObject<List<DigitalLink>>(responseStr)?.Select(d => d as DigitalLink).ToList() ?? new List<DigitalLink>();
                if (links.Count > 0)
                {
                    foreach (var link in links)
                    {
                        try
                        {
                            request = new HttpRequestMessage();
                            request.RequestUri = new Uri(link.link);
                            request.Method = HttpMethod.Get;

                            response = await httpClient.SendAsync(request);

                            if (response.IsSuccessStatusCode)
                            {
                                var json = (await response.Content.ReadFromJsonAsync<object>())?.ToString();

                                if (json is null)
                                    throw new NullReferenceException("Error parsing the digital link response: JSON value is null");

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
                                        return item;
                                    }
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
                string contentStr = await response.Content.ReadAsStringAsync();
                throw new HttpRequestException($"There was an error trying to fetch digital links from {relativeURL} - {contentStr} - {response.ToString()}");
            }

            return null;
        }
    }
}