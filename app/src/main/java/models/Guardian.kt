package models

import android.graphics.Bitmap
import android.net.Uri

data class Guardian(
    var uid:String? = null,
    var avatar: String? = null,
    var displayName:String? = null,
    var mobilNR:String? = null,
    var relationship:String? = "Guardian"
)