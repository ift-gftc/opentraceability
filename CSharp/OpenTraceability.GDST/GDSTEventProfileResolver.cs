using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;

namespace OpenTraceability.GDST
{
    public enum GDSTEventProfile
    {
        Unknown = 0,
        Fishing,
        ImmatureHarvest,
        Aggregation,
        Disaggregation,
        Shipping,
        TransshipmentShipping,
        Receiving,
        TransshipmentReceiving,
        Landing,
        Commingling,
        Processing,
        OnVesselProcessing,
        FeedProcessing,
        MatureHarvest,
        Decommission
    }

    public static class GDSTEventProfileResolver
    {
        public static GDSTEventProfile Resolve(IEvent evt, EPCISBaseDocument doc)
        {
            if (evt is GDSTCommissionEvent)
            {
                if (HasProductClassification(evt, doc, "wildCaught"))
                {
                    return GDSTEventProfile.Fishing;
                }

                if (HasProductClassification(evt, doc, "developing"))
                {
                    return GDSTEventProfile.ImmatureHarvest;
                }

                return GDSTEventProfile.Unknown;
            }

            if (evt is GDSTAggregationEvent)
            {
                return GDSTEventProfile.Aggregation;
            }

            if (evt is GDSTDisaggregationEvent)
            {
                return GDSTEventProfile.Disaggregation;
            }

            if (evt is GDSTShippingEvent)
            {
                string? source = GetLocationClassification(doc, evt.SourceList);
                string? destination = GetLocationClassification(doc, evt.DestinationList);

                if (IsClassification(source, "vessel") && IsClassification(destination, "vessel"))
                {
                    return GDSTEventProfile.TransshipmentShipping;
                }

                return GDSTEventProfile.Shipping;
            }

            if (evt is GDSTReceivingEvent)
            {
                string? source = GetLocationClassification(doc, evt.SourceList);
                string? destination = GetLocationClassification(doc, evt.DestinationList);

                if (IsClassification(source, "vessel") && IsClassification(destination, "vessel"))
                {
                    return GDSTEventProfile.TransshipmentReceiving;
                }

                if (IsClassification(source, "vessel") && IsClassification(destination, "landFacility"))
                {
                    return GDSTEventProfile.Landing;
                }

                return GDSTEventProfile.Receiving;
            }

            if (evt is GDSTTransformationEvent)
            {
                string? bizLocationClassification = GetLocationClassification(doc, evt.Location?.GLN?.ToString());
                if (IsClassification(bizLocationClassification, "vessel"))
                {
                    return GDSTEventProfile.OnVesselProcessing;
                }

                if (HasProductClassification(evt, doc, "feed", EventProductType.Output))
                {
                    return GDSTEventProfile.FeedProcessing;
                }

                if (HasProductClassification(evt, doc, "mature", EventProductType.Output))
                {
                    return GDSTEventProfile.MatureHarvest;
                }

                if (InputsAndOutputsHaveSameGTIN(evt))
                {
                    return GDSTEventProfile.Commingling;
                }

                return GDSTEventProfile.Processing;
            }

            if (evt is GDSTDecommissionEvent)
            {
                return GDSTEventProfile.Decommission;
            }

            return GDSTEventProfile.Unknown;
        }

        private static bool HasProductClassification(IEvent evt, EPCISBaseDocument doc, string classification, params EventProductType[] productTypes)
        {
            foreach (var product in evt.Products)
            {
                if (product.EPC?.GTIN is null)
                {
                    continue;
                }

                if (productTypes.Length > 0 && !productTypes.Contains(product.Type))
                {
                    continue;
                }

                GDSTTradeItem? tradeItem = doc.GetMasterData<GDSTTradeItem>(product.EPC.GTIN.ToString());
                if (tradeItem?.ProductClassification?.Any(c => IsClassification(c.Value, classification)) == true)
                {
                    return true;
                }
            }

            return false;
        }

        private static bool InputsAndOutputsHaveSameGTIN(IEvent evt)
        {
            var inputGTINs = evt.Products
                .Where(p => p.Type == EventProductType.Input && p.EPC?.GTIN is not null)
                .Select(p => p.EPC.GTIN.ToString().ToLowerInvariant())
                .Distinct()
                .OrderBy(p => p)
                .ToList();

            var outputGTINs = evt.Products
                .Where(p => p.Type == EventProductType.Output && p.EPC?.GTIN is not null)
                .Select(p => p.EPC.GTIN.ToString().ToLowerInvariant())
                .Distinct()
                .OrderBy(p => p)
                .ToList();

            return inputGTINs.Count > 0 && inputGTINs.SequenceEqual(outputGTINs);
        }

        private static string? GetLocationClassification(EPCISBaseDocument doc, List<EventSource> sources)
        {
            var location = sources.FirstOrDefault(s => s.ParsedType == EventSourceType.Location);
            return GetLocationClassification(doc, location?.Value);
        }

        private static string? GetLocationClassification(EPCISBaseDocument doc, List<EventDestination> destinations)
        {
            var location = destinations.FirstOrDefault(d => d.ParsedType == EventDestinationType.Location);
            return GetLocationClassification(doc, location?.Value);
        }

        private static string? GetLocationClassification(EPCISBaseDocument doc, string? locationID)
        {
            if (string.IsNullOrWhiteSpace(locationID))
            {
                return null;
            }

            GDSTLocation? location = doc.GetMasterData<GDSTLocation>(locationID!);
            return location?.LocationClassification?.FirstOrDefault()?.Value;
        }

        private static bool IsClassification(string? value, string expected)
        {
            return Normalize(value) == Normalize(expected);
        }

        private static string Normalize(string? value)
        {
            if (string.IsNullOrWhiteSpace(value))
            {
                return string.Empty;
            }

            return new string(value.Where(char.IsLetterOrDigit).ToArray()).ToLowerInvariant();
        }
    }
}
