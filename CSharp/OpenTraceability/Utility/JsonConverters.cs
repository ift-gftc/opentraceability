using Newtonsoft.Json;
using OpenTraceability.Models.Identifiers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public class EPCConverter : JsonConverter<EPC>
    {
        public override void WriteJson(JsonWriter writer, EPC? value, JsonSerializer serializer)
        {
            string? strValue = value?.ToString();
            if (strValue != null)
            {
                writer.WriteValue(strValue);
            }
        }

        public override EPC? ReadJson(JsonReader reader, Type objectType, EPC? existingValue, bool hasExistingValue, JsonSerializer serializer)
        {
            string? strValue = reader.Value?.ToString();
            if (strValue != null)
            {
                EPC epc = new EPC(strValue);
                return epc;
            }
            else
            {
                return null;
            }
        }
    }
}
