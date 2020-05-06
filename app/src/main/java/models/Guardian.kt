package models

data class Guardian(var firstname:String? = null,
                    var lastname:String? = null,
                    var mobilNR:String? = null,
                    var email:String? = null,
                    var relationship:String? = "Unspecified"
)