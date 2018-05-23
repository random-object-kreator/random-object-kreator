
package ko.kreator

import java.io.Serializable


data class SupportedUserId(val id: String) : Serializable

sealed class SealedClassWithFields {
    abstract val supportedUserId: SupportedUserId
    abstract val name: String
    abstract val photo: String
}

//data class ConcreteSealedClassWithFields(
//        override val supportedUserId: SupportedUserId, override val name: String, override val photo: String
//) : SealedClassWithFields()
