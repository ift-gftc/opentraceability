package opentraceability.queries;

import java.util.ArrayList;
import java.util.List;

import opentraceability.models.events.EPCISQueryDocument;

public class EPCISQueryResults {

    public EPCISQueryDocument Document = null;

    public List<EPCISQueryStackTraceItem> StackTrace = new ArrayList<>();

    public List<EPCISQueryError> Errors = new ArrayList<>();

    public void merge(EPCISQueryResults results) {
        for (EPCISQueryStackTraceItem el : results.StackTrace) {
            this.StackTrace.add(el);
        }

        for (EPCISQueryError el : results.Errors) {
            this.Errors.add(el);
        }

        if (this.Document == null) {
            this.Document = results.Document;
        } else if (results.Document != null) {
            this.Document.merge(results.Document);
        }
    }
}