using Newtonsoft.Json;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
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
        public static async Task ResolveMasterData(MasterDataQueryOptions options, EPCISBaseDocument doc)
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        ResolveTradeitem(options, p.EPC.GTIN, doc);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc);

                foreach (var source in evt.SourceList)
                {
                    if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty(options, pgln, doc);
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
                            await ResolveTradingParty(options, pgln, doc);
                        }
                    }
                }
            }
        }

        public static async Task ResolveMasterData<TTradeitem, TLocation, TTradingParty>(MasterDataQueryOptions options, EPCISBaseDocument doc) where TTradeitem: Tradeitem, new() where TLocation: Location, new() where TTradingParty : TradingParty, new()
        {
            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                foreach (var p in evt.Products)
                {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance)
                    {
                        ResolveTradeitem<TTradeitem>(options, p.EPC.GTIN, doc);
                    }
                }

                await ResolveLocation(options, evt.Location?.GLN, doc);

                foreach (var source in evt.SourceList)
                {
                    if (source.Type == OpenTraceability.Constants.EPCIS.URN.SDT_Owner)
                    {
                        var pgln = new PGLN(source.Value ?? throw new Exception("source in event source list has NULL value."));
                        if (pgln != null)
                        {
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc);
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
                            await ResolveTradingParty<TTradingParty>(options, pgln, doc);
                        }
                    }
                }
            }
        }

        public static async Task ResolveTradeitem(MasterDataQueryOptions options, GTIN? gtin, EPCISBaseDocument doc)
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Tradeitem)) ?? typeof(Tradeitem);
                    var ti = (await ResolveMasterDataItem(t, options, $"/01/{gtin}?linkType=gs1:masterData")) as Tradeitem;
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation(MasterDataQueryOptions options, GLN? gln, EPCISBaseDocument doc)
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(Location)) ?? typeof(Location);
                    var l = (await ResolveMasterDataItem(t, options, $"/414/{gln}?linkType=gs1:masterData")) as Location;
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty(MasterDataQueryOptions options, PGLN? pgln, EPCISBaseDocument doc)
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    Type t = Setup.GetMasterDataTypeDefault(typeof(TradingParty)) ?? typeof(TradingParty);
                    var tp = (await ResolveMasterDataItem(t, options, $"/417/{pgln}?linkType=gs1:masterData")) as TradingParty;
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task ResolveTradeitem<T>(MasterDataQueryOptions options, GTIN? gtin, EPCISBaseDocument doc) where T : Tradeitem
        {
            if (gtin != null)
            {
                if (doc.GetMasterData<Tradeitem>(gtin.ToString()) == null)
                {
                    var ti = await ResolverMasterDataItem<T>(options, $"/01/{gtin}?linkType=gs1:masterData");
                    if (ti != null)
                    {
                        doc.MasterData.Add(ti);
                    }
                }
            }
        }

        public static async Task ResolveLocation<T>(MasterDataQueryOptions options, GLN? gln, EPCISBaseDocument doc) where T : Location
        {
            if (gln != null)
            {
                if (doc.GetMasterData<Location>(gln.ToString()) == null)
                {
                    var l = await ResolverMasterDataItem<T>(options, $"/414/{gln}?linkType=gs1:masterData");
                    if (l != null)
                    {
                        doc.MasterData.Add(l);
                    }
                }
            }
        }

        public static async Task ResolveTradingParty<T>(MasterDataQueryOptions options, PGLN? pgln, EPCISBaseDocument doc) where T : TradingParty
        {
            if (pgln != null)
            {
                if (doc.GetMasterData<TradingParty>(pgln.ToString()) == null)
                {
                    var tp = await ResolverMasterDataItem<T>(options, $"/417/{pgln}?linkType=gs1:masterData");
                    if (tp != null)
                    {
                        doc.MasterData.Add(tp);
                    }
                }
            }
        }

        public static async Task<Tradeitem?> ResolveTradeitem(MasterDataQueryOptions options, GTIN? gtin)
        {
            if (gtin != null)
            {
                var ti = await ResolverMasterDataItem<Tradeitem>(options, $"/01/{gtin}?linkType=gs1:masterData");
                if (ti != null)
                {
                    return ti;
                }
            }
            return null;
        }

        public static async Task<Location?> ResolveLocation(MasterDataQueryOptions options, GLN? gln)
        {
            if (gln != null)
            {
                var l = await ResolverMasterDataItem<Location>(options, $"/414/{gln}?linkType=gs1:masterData");
                if (l != null)
                {
                    return l;
                }
            }
            return null;
        }

        public static async Task<TradingParty?> ResolveTradingParty(MasterDataQueryOptions options, PGLN? pgln)
        {
            if (pgln != null)
            {
                var tp = await ResolverMasterDataItem<TradingParty>(options, $"/417/{pgln}?linkType=gs1:masterData");
                if (tp != null)
                {
                    return tp;
                }
            }
            return null;
        }

        public static async Task<T?> ResolverMasterDataItem<T>(MasterDataQueryOptions options, string relativeURL) where T : IVocabularyElement
        {
            var response = await ResolveMasterDataItem(typeof(T), options, relativeURL);
            if (response == null)
            {
                return default(T);
            }
            else
            {
                return (T)response;
            }
        }

        public static async Task<object?> ResolveMasterDataItem(Type type, MasterDataQueryOptions options, string relativeURL)
        {
            if (options.URL == null)
            {
                throw new Exception("options.Uri is null on the MasterDataQueryOptions");
            }

            HttpRequestMessage request = new HttpRequestMessage();
            request.RequestUri = new Uri(options.URL + relativeURL);
            request.Method = HttpMethod.Get;

            if (!string.IsNullOrWhiteSpace(options.XAPIKey))
            {
                request.Headers.Add("X-API-Key", options.XAPIKey);
            }

            if (!string.IsNullOrWhiteSpace(options.BearerToken))
            {
                request.Headers.Add("Authorization", "Bearer " + options.BearerToken);
            }

            using var clientItem = HttpClientPool.GetClient();
            var httpClient = clientItem.Value;
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

                            if (link.authRequired == true)
                            {
                                if (!string.IsNullOrWhiteSpace(options.XAPIKey))
                                {
                                    request.Headers.Add("X-API-Key", options.XAPIKey);
                                }

                                if (!string.IsNullOrWhiteSpace(options.BearerToken))
                                {
                                    request.Headers.Add("Authorization", "Bearer " + options.BearerToken);
                                }
                            }

                            response = await httpClient.SendAsync(request);

                            if (response.IsSuccessStatusCode)
                            {
                                string json = await response.Content.ReadAsStringAsync();
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
                throw new HttpRequestException($"There was an error trying to fetch digital links from {relativeURL}");
            }

            return null;
        }
    }
}
