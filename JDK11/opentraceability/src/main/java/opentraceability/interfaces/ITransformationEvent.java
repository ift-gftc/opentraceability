package opentraceability.interfaces;

public interface ITransformationEvent extends IEvent {
    String getTransformationID();
    void setTransformationID(String transformationID);
}