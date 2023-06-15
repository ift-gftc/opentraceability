package opentraceability.mappers;

import opentraceability.interfaces.IMasterDataMapper;
import opentraceability.mappers.masterdata.GS1VocabJsonMapper;

public class MasterDataMappers {
    public IMasterDataMapper GS1WebVocab = new GS1VocabJsonMapper();
}