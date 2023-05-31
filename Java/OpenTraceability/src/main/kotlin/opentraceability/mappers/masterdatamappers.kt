package mappers

import interfaces.IMasterDataMapper
import mappers.masterdata.GS1VocabJsonMapper

class MasterDataMappers {
    var GS1WebVocab: IMasterDataMapper = GS1VocabJsonMapper()
}
