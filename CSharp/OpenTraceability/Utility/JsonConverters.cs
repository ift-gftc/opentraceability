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

    public class GTINConverter : JsonConverter<GTIN>
    {
        public override void WriteJson(JsonWriter writer, GTIN? value, JsonSerializer serializer)
        {
            string? strValue = value?.ToString();
            if (strValue != null)
            {
                writer.WriteValue(strValue);
            }
        }

        public override GTIN? ReadJson(JsonReader reader, Type objectType, GTIN? existingValue, bool hasExistingValue, JsonSerializer serializer)
        {
            string? strValue = reader.Value?.ToString();
            if (strValue != null)
            {
                GTIN gtin = new GTIN(strValue);
                return gtin;
            }
            else
            {
                return null;
            }
        }
    }

    public class GLNConverter : JsonConverter<GLN>
    {
        public override void WriteJson(JsonWriter writer, GLN? value, JsonSerializer serializer)
        {
            string? strValue = value?.ToString();
            if (strValue != null)
            {
                writer.WriteValue(strValue);
            }
        }

        public override GLN? ReadJson(JsonReader reader, Type objectType, GLN? existingValue, bool hasExistingValue, JsonSerializer serializer)
        {
            string? strValue = reader.Value?.ToString();
            if (strValue != null)
            {
                GLN gln = new GLN(strValue);
                return gln;
            }
            else
            {
                return null;
            }
        }
    }

    public class PGLNConverter : JsonConverter<PGLN>
    {
        public override void WriteJson(JsonWriter writer, PGLN? value, JsonSerializer serializer)
        {
            string? strValue = value?.ToString();
            if (strValue != null)
            {
                writer.WriteValue(strValue);
            }
        }

        public override PGLN? ReadJson(JsonReader reader, Type objectType, PGLN? existingValue, bool hasExistingValue, JsonSerializer serializer)
        {
            string? strValue = reader.Value?.ToString();
            if (strValue != null)
            {
                PGLN pgln = new PGLN(strValue);
                return pgln;
            }
            else
            {
                return null;
            }
        }
    }
}
