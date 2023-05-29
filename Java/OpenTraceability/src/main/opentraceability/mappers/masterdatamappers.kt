package opentraceability.mappers

import opentraceability.interfaces.IMasterDataMapper
import opentraceability.mappers.masterdata.GS1VocabJsonMapper

class MasterDataMappers {
    var GS1WebVocab: IMasterDataMapper = GS1VocabJsonMapper()
}
