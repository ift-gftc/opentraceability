package opentraceability;

import opentraceability.interfaces.IEvent;
import opentraceability.models.events.EventAction;
import opentraceability.models.events.EventType;


import java.util.List;
import java.util.ArrayList;

public class OpenTraceabilityEventProfile {

    public int getSpecificityScore() {
        int score = 1;

        if (Action != null) {
            score++;
        }

        if (BusinessStep != null && BusinessStep.length() > 0) {
            score++;
        }

        if (KDEProfiles != null && KDEProfiles.size() > 0) {
            score += KDEProfiles.size();
        }

        return score;
    }

    public EventType EventType;
    public EventAction Action = null;
    public String BusinessStep = "";
    public Class EventClassType;
    public List<OpenTraceabilityEventKDEProfile> KDEProfiles = null;

    public OpenTraceabilityEventProfile() {
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType) {
        EventType = eventType;
        EventClassType = eventClassType;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, String businessStep) {
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, EventAction action) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, String businessStep, EventAction action) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, List<OpenTraceabilityEventKDEProfile> kdeProfiles) {
        KDEProfiles = new ArrayList<OpenTraceabilityEventKDEProfile>();
        KDEProfiles.addAll(kdeProfiles);
        EventType = eventType;
        EventClassType = eventClassType;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, String businessStep, List<OpenTraceabilityEventKDEProfile> kdeProfiles) {
        KDEProfiles = new ArrayList<OpenTraceabilityEventKDEProfile>();
        KDEProfiles.addAll(kdeProfiles);
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, EventAction action, List<OpenTraceabilityEventKDEProfile> kdeProfiles) {
        KDEProfiles = new ArrayList<OpenTraceabilityEventKDEProfile>();
        KDEProfiles.addAll(kdeProfiles);
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }

    public OpenTraceabilityEventProfile(Class eventClassType, EventType eventType, String businessStep, EventAction action, List<OpenTraceabilityEventKDEProfile> kdeProfiles) {
        KDEProfiles = new ArrayList<OpenTraceabilityEventKDEProfile>();
        KDEProfiles.addAll(kdeProfiles);
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    @Override
    public String toString() {
        String eventTypes = "";

        if (KDEProfiles != null && KDEProfiles.size() > 0) {
            List<String> kdeProfileNames = new ArrayList<String>();
            for (OpenTraceabilityEventKDEProfile profile : KDEProfiles) {
                kdeProfileNames.add("'" + profile.toString() + "'");
            }
            eventTypes = String.join(":", kdeProfileNames);
        }

        return EventType.toString() + ":" + ((Action != null) ? Action.toString() : "") + ":" + BusinessStep + ":" + eventTypes;
    }
}