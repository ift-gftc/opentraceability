interface IEvent {
    fun get_EventID(): URI
    fun set_EventID(URI value): Void
    fun get_CertificationInfo(): String
    fun set_CertificationInfo(String value): Void
    fun get_EventTime(): OffsetDateTime?
    fun set_EventTime(OffsetDateTime? value): Void
    fun get_EventTimeZoneOffset(): TimeSpan?
    fun set_EventTimeZoneOffset(TimeSpan? value): Void
    fun get_RecordTime(): OffsetDateTime?
    fun set_RecordTime(OffsetDateTime? value): Void
    fun get_EventType(): EventType
    fun get_Action(): EventAction?
    fun set_Action(EventAction? value): Void
    fun get_BusinessStep(): URI
    fun set_BusinessStep(URI value): Void
    fun get_Disposition(): URI
    fun set_Disposition(URI value): Void
    fun get_PersistentDisposition(): PersistentDisposition
    fun set_PersistentDisposition(PersistentDisposition value): Void
    fun get_ErrorDeclaration(): ErrorDeclaration
    fun set_ErrorDeclaration(ErrorDeclaration value): Void
    fun get_Location(): EventLocation
    fun set_Location(EventLocation value): Void
    fun get_ReadPoint(): EventReadPoint
    fun set_ReadPoint(EventReadPoint value): Void
    fun get_BizTransactionList(): List<EventBusinessTransaction>
    fun set_BizTransactionList(List<EventBusinessTransaction> value): Void
    fun get_SourceList(): List<EventSource>
    fun set_SourceList(List<EventSource> value): Void
    fun get_DestinationList(): List<EventDestination>
    fun set_DestinationList(List<EventDestination> value): Void
    fun get_KDEs(): List<IEventKDE>
    fun get_SensorElementList(): List<SensorElement>
    fun set_SensorElementList(List<SensorElement> value): Void
    fun get_Products(): ReadOnlyCollection`1
    fun GetKDE(String ns, String name): T
    fun GetKDE(): T
    fun AddProduct(EventProduct product): Void
    fun get_CertificationList(): CertificationList
    fun set_CertificationList(CertificationList value): Void
    fun GetILMD(): EventILMD
}
