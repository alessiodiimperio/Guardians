package models

import com.google.android.gms.maps.model.LatLng
import java.time.format.DateTimeFormatter


data class AlarmObject(val uid:String? = null, val location: MyLatLng? = null, val timestamp:String? = null)