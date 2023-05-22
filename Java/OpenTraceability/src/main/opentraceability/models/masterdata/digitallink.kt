package models.masterdata
import java.lang.reflect.Type
class DigitalLink {
    var link: String = ""
    var title: String = ""
    var linkType: String = ""
    var ianaLanguage: String = ""
    var context: String = ""
    var mimeType: String = ""
    var active: Boolean = Boolean()
    var fwqs: Boolean = Boolean()
    var defaultLinkType: Boolean = Boolean()
    var defaultIanaLanguage: Boolean = Boolean()
    var defaultContext: Boolean = Boolean()
    var defaultMimeType: Boolean = Boolean()
    var identifier: String = ""
    var authRequired: Boolean = Boolean()
    companion object{
    }
}
