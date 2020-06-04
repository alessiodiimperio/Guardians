package models

data class Guardian(
    var uid:String? = null,
    var avatar: String? = null,
    var displayName:String? = null,
    var phoneNumber:String? = null,
    var relationship:String? = "Guardian",
    var email:String? = null
)