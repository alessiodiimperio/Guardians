package models


import Alarm.ALARM_FRAGMENT
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import se.diimperio.guardians.RequestCodes
import java.lang.Exception

object UserManager {

    val USER_MANAGER: String = "USER_MANAGER"

    var db = FirebaseFirestore.getInstance()
    val currentUser = User(null,null, null, null, null, null, mutableListOf())

    val token = FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {result->
        if(result != null){
            currentUser.token = result.token
            syncChangesToFirebase()
        }
    }

    lateinit var locationProviderClient :FusedLocationProviderClient
    lateinit var locationCallBack: LocationCallback

    fun syncChangesToFirebase() {
        val userCollection = db.collection("users")
        val profilePath = currentUser.uid ?: return

        userCollection.document(profilePath).set(currentUser)
            .addOnSuccessListener {
                Log.d(USER_MANAGER, "Sync Success")
            }
            .addOnFailureListener { error ->
                Log.d(USER_MANAGER, "Sync error: ${error.message}")
            }
    }

    fun addGuardian(guardian: Guardian) {
        currentUser.guardians.add(guardian)
        syncChangesToFirebase()
    }

    fun removeGuardian(position: Int) {
        currentUser.guardians.removeAt(position)
        syncChangesToFirebase()
    }

    fun startUsersLocationUpdates(activity: Activity, context: Context) {
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {


                locationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.create().apply {
                    interval = 10000
                    fastestInterval = 5000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                locationCallBack = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        locationResult ?: return

                        val location = locationResult.locations[locationResult.locations.size - 1]
                        currentUser.location = MyLatLng(location.latitude, location.longitude)

                        Log.d(USER_MANAGER, "repeating? : ${currentUser.location?.latitude} & ${currentUser.location?.longitude}")
                    }
                }
                locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }
        } else {
            requestPermissions(activity)
        }

    }
    fun stopUserLocationUpdates(){
            val locationsStop = locationProviderClient.removeLocationUpdates(locationCallBack)
        if (locationsStop.isSuccessful) {
            Log.d(USER_MANAGER, "Location Services Stop Successfull")
        }
        if (locationsStop.exception != null) {
            Log.d(USER_MANAGER, "Location Stop Error: ${locationsStop.exception}")
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        return checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            RequestCodes.REQUEST_LOCATION
        )
    }
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun notifyGuardians(testMode:Boolean) {
        AlarmManager.uploadAlarmObjectToServer()

        val alertLocation = currentUser.location
        val alertLatitude = alertLocation?.latitude
        val alertLongtitude = alertLocation?.longitude

        val smsManager = SmsManager.getDefault()

        currentUser.guardians.forEach{ guardian ->
            val alert = if (testMode) "TESTMODE: ${currentUser.name} is testing Guardian's safety alarm from this location: https://www.google.com/maps/search/?api=1&query=$alertLatitude,$alertLongtitude" else "${currentUser.name}'s safety alarm has been triggered at the following location: https://www.google.com/maps/search/?api=1&query=$alertLatitude,$alertLongtitude"
                smsManager.sendTextMessage(guardian.mobilNR, null, alert, null, null)
                Log.d(USER_MANAGER, alert)
        }
    }
    fun getUserLocation(activity: Activity, context: Context, onLocationResult: (location : MyLatLng) -> Unit) {
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                locationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.create().apply {
                    numUpdates = 1
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                locationCallBack = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)

                        //if null return
                        locationResult ?: return

                        val location = locationResult.locations[locationResult.locations.size - 1]
                        currentUser.location = MyLatLng(location.latitude, location.longitude)

                        onLocationResult.invoke(MyLatLng(location.latitude,location.longitude))
                        Log.d(USER_MANAGER, "Get one off user location: ${currentUser.location?.latitude} & ${currentUser.location?.longitude}")
                        locationProviderClient.removeLocationUpdates(locationCallBack)
                    }
                }
                locationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }
        } else {
            requestPermissions(activity)
        }
    }
}