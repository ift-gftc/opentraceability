package opentraceability.gdst.MasterData

import opentraceability.models.masterdata.*
import opentraceability.utility.*

data class GDSTLocation(
    val vesselFlagState: Country?
) : Location(){

}