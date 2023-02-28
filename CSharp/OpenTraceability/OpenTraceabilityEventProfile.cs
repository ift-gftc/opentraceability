using Newtonsoft.Json.Bson;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    /// <summary>
    /// An event profile is used to determine the C# class to represent the EPCIS event when deserializing it from a data-format language 
    /// such as XML/JSON/JSON-LD.
    /// </summary>
    public class OpenTraceabilityEventProfile
    {
        /// <summary>
        /// This returns a score based on how specific the profile is. More specific 
        /// profiles are selected first before least specific profiles.
        /// </summary>
        public int SpecificityScore 
        { 
            get
            {
                int score = 1;
                if (Action != null)
                {
                    score++;
                }
                if (BusinessStep != null)
                {
                    score++;
                }
                if (KDEProfiles != null)
                {
                    score += KDEProfiles.Count;
                }
                return score;
            } 
        }

        /// <summary>
        /// This should either be ObjectEvent, TransformationEvent, TransactionEvent, AggregationEvent, or AssociationEvent.
        /// </summary>
        public EventType EventType { get; set; }

        /// <summary>
        /// This will match the action on the event to the this action for profiling.
        /// </summary>
        public EventAction? Action { get; set; }

        /// <summary>
        /// This will match the business step on the event to this action for profiling.
        /// </summary>
        public string? BusinessStep { get; set; }

        /// <summary>
        /// This is the C# class to deserialize the event into.
        /// </summary>
        public Type EventClassType { get; set; }

        /// <summary>
        /// One or more KDEs required to be present on the event to be profiled as this event.
        /// </summary>
        public List<OpenTraceabilityEventKDEProfile>? KDEProfiles { get; set; }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType)
        {
            EventType = eventType;
            EventClassType = eventClassType;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, string businessStep)
        {
            EventType = eventType;
            EventClassType = eventClassType;
            BusinessStep = businessStep;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, EventAction action)
        {
            EventType = eventType;
            EventClassType = eventClassType;
            Action = action;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, string businessStep, EventAction action)
        {
            EventType = eventType;
            EventClassType = eventClassType;
            Action = action;
            BusinessStep = businessStep;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, List<OpenTraceabilityEventKDEProfile> kdeProfiles)
        {
            KDEProfiles = kdeProfiles;
            EventType = eventType;
            EventClassType = eventClassType;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, string businessStep, List<OpenTraceabilityEventKDEProfile> kdeProfiles)
        {
            KDEProfiles = kdeProfiles;
            EventType = eventType;
            EventClassType = eventClassType;
            BusinessStep = businessStep;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, EventAction action, List<OpenTraceabilityEventKDEProfile> kdeProfiles)
        {
            KDEProfiles = kdeProfiles;
            EventType = eventType;
            EventClassType = eventClassType;
            Action = action;
        }

        public OpenTraceabilityEventProfile(Type eventClassType, EventType eventType, string businessStep, EventAction action, List<OpenTraceabilityEventKDEProfile> kdeProfiles)
        {
            KDEProfiles = kdeProfiles;
            EventType = eventType;
            EventClassType = eventClassType;
            Action = action;
            BusinessStep = businessStep;
        }
    }
}
