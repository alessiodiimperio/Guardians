package models

import com.google.android.gms.maps.model.LatLng

class MyLatLng(val latitude:Double? = null, val longitude:Double? = null) {

    fun convertToGoogleLatLng():LatLng?{
        if (latitude != null && longitude != null){
        return LatLng(latitude,longitude)
    } else {
            return null
        }
    }
}