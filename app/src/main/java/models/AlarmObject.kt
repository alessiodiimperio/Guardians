package models


data class AlarmObject(val uid:String? = null, val location: MyLatLng? = null, val timestamp:String? = null, val guardianUids:List<String> = mutableListOf(), val testmode:Boolean = false)