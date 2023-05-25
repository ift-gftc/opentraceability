import java.lang.reflect.Type
class Setup {
    companion object{

        internal var Profiles : ArrayList<OpenTraceabilityEventProfile> = ArrayList<OpenTraceabilityEventProfile>()
        internal var MasterDataTypes : MutableMap<String, Type> = mutableMapOf<String, Type>()
        internal var MasterDataTypeDefault : MutableMap<Type, Type> = mutableMapOf<Type, Type>()

        fun Initialize() {
            TODO("Not yet implemented")
        }

        fun RegisterEventProfile(profile: OpenTraceabilityEventProfile) {
            TODO("Not yet implemented")
        }

        fun RegisterMasterDataType(defaultFor: Type) {
            TODO("Not yet implemented")
        }


        fun RegisterMasterDataType() {
            TODO("Not yet implemented")
        }

        fun GetMasterDataTypeDefault(type: Type): Type {
            TODO("Not yet implemented")
        }
    }
}
