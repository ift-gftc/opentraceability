package opentraceability.models.events;

import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.models.common.StandardBusinessDocumentHeader;
import opentraceability.models.identifiers.EPC;
import opentraceability.queries.EPCISQueryParameters;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class EPCISBaseDocument {
    public EPCISVersion epcisVersion = null;
    public OffsetDateTime creationDate = null;
    public StandardBusinessDocumentHeader header = null;
    public ArrayList<IEvent> events = new ArrayList<IEvent>();
    public ArrayList<IVocabularyElement> masterData = new ArrayList<IVocabularyElement>();
    public HashMap<String, String> namespaces = new HashMap<String, String>();
    public ArrayList<String> contexts = new ArrayList<String>();
    public HashMap<String, String> attributes = new HashMap<String, String>();

    public <T extends IVocabularyElement> ArrayList<T> searchMasterData(Class<T> type) {
        ArrayList<T> result = new ArrayList<>();
        for (IVocabularyElement element : this.masterData) {
            if (type.isAssignableFrom(element.getClass())) {
                result.add((T) element);
            }
        }
        return result;
    }

    public <T extends IVocabularyElement> T searchMasterData(String id, Class<T> type) {
        for (IVocabularyElement element : this.masterData) {
            if (type.isAssignableFrom(element.getClass()) && element.getId().equals(id)) {
                return (T) element;
            }
        }
        return null;
    }

    public void merge(EPCISBaseDocument data) {
        for (IEvent e : data.events) {
            boolean found = false;
            for (IEvent e2 : this.events) {
                if (e.eventID.equals(e2.eventID)) {
                    if (e.errorDeclaration == null && e2.errorDeclaration != null) {
                        this.events.remove(e);
                        this.events.add(e2);
                    }
                    found = true;
                }
            }

            if (!found) {
                this.events.add(e);
            }

            for (IVocabularyElement element : data.masterData) {
                Stream<IVocabularyElement> singles = this.masterData.stream().filter(x -> x.getId().equals(element.getId()));
                if (singles.count() == 0) {
                    this.masterData.add(element);
                }
            }
        }
    }

    public List<IEvent> filterEvents(EPCISQueryParameters parameters) throws Exception {
        List<IEvent> result = new ArrayList<>();

        for (IEvent evt : this.events) {

            if (parameters.query.GE_eventTime != null) {
                if (evt.eventTime == null || evt.eventTime.isBefore(parameters.query.GE_eventTime)) {
                    continue;
                }
            }

            if (parameters.query.LE_eventTime != null) {
                if (evt.eventTime == null || evt.eventTime.isAfter(parameters.query.LE_eventTime)) {
                    continue;
                }
            }

            if (parameters.query.GE_recordTime != null) {
                if (evt.recordTime == null || evt.recordTime.isBefore(parameters.query.GE_recordTime)) {
                    continue;
                }
            }

            if (parameters.query.LE_recordTime != null) {
                if (evt.recordTime == null || evt.recordTime.isAfter(parameters.query.LE_recordTime)) {
                    continue;
                }
            }

            if (parameters.query.EQ_bizStep != null && !parameters.query.EQ_bizStep.isEmpty()) {

                if (!HasUriMatch(evt.businessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-",
                        "urn:epcglobal:cbv:bizstep:")) {
                    continue;
                }
            }

            if (parameters.query.EQ_bizLocation != null && !parameters.query.EQ_bizLocation.isEmpty()) {
                boolean found = false;
                for (String eqBiz : parameters.query.EQ_bizLocation) {
                    if (evt.location != null && evt.location.gln != null
                            && eqBiz.toString().equalsIgnoreCase(evt.location.gln.toString())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }

            }

            if (parameters.query.MATCH_anyEPC != null && !parameters.query.MATCH_anyEPC.isEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPC)) {
                    continue;
                }
            }

            if (parameters.query.MATCH_anyEPCClass != null && !parameters.query.MATCH_anyEPCClass.isEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPCClass)) {
                    continue;
                }
            }

            if (parameters.query.MATCH_epc != null && !parameters.query.MATCH_epc.isEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_epc, EventProductType.Reference, EventProductType.Child)) {
                    continue;
                }
            }

            if (parameters.query.MATCH_epcClass != null && !parameters.query.MATCH_epcClass.isEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_epcClass, EventProductType.Reference,
                        EventProductType.Child)) {
                    continue;
                }
            }

            result.add(evt);
        }
        return result;
    }

    private boolean HasMatch(IEvent evt, List<String> epcs, EventProductType... allowedTypes) throws Exception {
        for (String epcMatchStr : epcs) {
            EPC epcMatch = new EPC(epcMatchStr);
            for (EventProduct product : evt.getProducts()) {
                if (allowedTypes.length == 0
                        || (product.Type != null && isInAllowedTypes(product.Type.toString(), allowedTypes))) {
                    if (epcMatch.Matches(product.EPC)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean HasUriMatch(URI uri, List<String> filter, String prefix, String replacePrefix) {
        for (int i = 0; i < filter.size(); i++) {
            String bizStep = filter.get(i);
            URI u = URI.create(bizStep);
            if (u == null) {
                filter.set(i, replacePrefix + bizStep);
            } else if (bizStep.startsWith(prefix)) {
                filter.set(i, replacePrefix + bizStep.split("-")[2]);
            }
        }
        if (uri != null) {
            URI bizStep = URI.create(uri.toString());
            if (bizStep.toString().startsWith(prefix)) {
                bizStep = URI.create(
                        replacePrefix + uri.toString().substring(uri.toString().lastIndexOf("-") + 1));
            }
            List<URI> filterUris = new ArrayList<>();
            for (String s : filter) {
                filterUris.add(URI.create(s));
            }
            URI finalBizStep = bizStep;
            return filterUris.stream().anyMatch(u -> u.toString().equalsIgnoreCase(finalBizStep.toString()));
        } else {
            return false;
        }
    }

    private boolean isInAllowedTypes(String type, EventProductType... allowedTypes) {
        for (EventProductType _type : allowedTypes) {
            if (_type.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }
}