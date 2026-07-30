using OpenTraceability.Interfaces;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace OpenTraceability.Models.Events
{
    public enum EventErrorType
    {
        Unknown = 0,

        [Description("urn:epcis:errorType:incorrect_data")]
        IncorrectData = 1,

        [Description("urn:epcis:errorType:did_not_occur")]
        DidNotOccur = 2
    }

    public class ErrorDeclaration
    {
        [OpenTraceabilityJson("reason")]
        [OpenTraceability("@type")]
        public Uri? Reason { get; set; }

        [OpenTraceability("declarationTime")]
        public DateTimeOffset? DeclarationTime { get; set; }

        [OpenTraceabilityArray("correctiveEventID")]
        [OpenTraceability("correctiveEventIDs")]
        public List<string> CorrectingEventIDs { get; set; } = new List<string>();

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}