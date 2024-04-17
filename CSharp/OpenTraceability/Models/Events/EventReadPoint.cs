using OpenTraceability.Utility.Attributes;
using System;

namespace OpenTraceability.Models.Events
{
    public class EventReadPoint
    {
        [OpenTraceability("id")]
        public Uri ID { get; set; }
    }
}