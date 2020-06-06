package Managers


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import models.Guardian
import models.MyLatLng
import models.User
import models.emailToUidObject
import se.diimperio.guardians.RequestCodes
import java.util.*


object UserManager {

    val USER_MANAGER: String = "USER_MANAGER"

    var db = FirebaseFirestore.getInstance()
    val currentUser =
        User(null, null, null, null, null, null, mutableListOf())

    val token = FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { result ->
        if (result != null) {
            currentUser.token = result.token
            syncChangesToFirebase()
        }
    }

    lateinit var locationProviderClient: FusedLocationProviderClient
    lateinit var locationCallBack: LocationCallback
    lateinit var singleLocationProvider: FusedLocationProviderClient
    lateinit var singleLocationCallback: LocationCallback

    fun syncGuardianUids(callback: (() -> Unit)) {
        val emailToUidRef = db.collection("emailToUid")

        var requestCount = 0
        currentUser.guardians.forEach { guardian ->

            val email = guardian.email

            if (email != null && email.isNotEmpty()) {

                emailToUidRef.document(email).get().addOnSuccessListener { document ->
                    Log.d(USER_MANAGER, "Found matching uid for $email")
                    if (document != null) {
                        val emailToUidObject = document.toObject(emailToUidObject::class.java)

                        if (emailToUidObject != null) {

                            val guardianUID = emailToUidObject.uid
                            if (guardianUID != null) {
                                guardian.uid = guardianUID
                                requestCount += 1

                                if (requestCount == currentUser.guardians.size) {
                                    Log.d(USER_MANAGER, "syncing after request")
                                    callback.invoke()
                                }
                                Log.d(USER_MANAGER, "UID: $guardianUID")
                                Log.d(USER_MANAGER, "${guardian.uid}")
                            }
                        }
                    }
                }.addOnFailureListener {
                    Log.d(USER_MANAGER, "No UID object found for ${guardian.email}")
                    requestCount += 1
                    if (requestCount == currentUser.guardians.size) {
                        Log.d(USER_MANAGER, "syncing after request")
                        callback.invoke()
                    }
                }
            } else {
                requestCount += 1
                if (requestCount == currentUser.guardians.size) {
                    Log.d(USER_MANAGER, "syncing after request")
                    callback.invoke()
                }
            }
        }
    }

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
        syncGuardianUids {
            syncChangesToFirebase()
        }
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

                        locationResult ?: return //if null return

                        val location = locationResult.locations[locationResult.locations.size - 1]
                        currentUser.location =
                            MyLatLng(
                                location.latitude,
                                location.longitude
                            )

                        Log.d(
                            USER_MANAGER,
                            "Location updates: ${currentUser.location?.latitude} & ${currentUser.location?.longitude}"
                        )
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

    fun stopUserLocationUpdates() {
        locationProviderClient.removeLocationUpdates(
            locationCallBack
        ).addOnSuccessListener {
            Log.d(USER_MANAGER, "Removed location updates Success")
        }.addOnFailureListener { error ->
            Log.d(USER_MANAGER, "Error stoping location $error")
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

    fun notifyGuardians(testmode: Boolean) {
        AlarmManager.uploadAlarmObjectToServer(testmode)

        val alertLocation = currentUser.location
        val alertLatitude = alertLocation?.latitude
        val alertLongtitude = alertLocation?.longitude

        val smsManager = SmsManager.getDefault()

        currentUser.guardians.forEach { guardian ->
            val alert =
                if (testmode) "TESTMODE: ${currentUser.name} is testing Guardian's safety alarm from this location: https://www.google.com/maps/search/?api=1&query=$alertLatitude,$alertLongtitude" else "${currentUser.name}'s safety alarm has been triggered at the following location: https://www.google.com/maps/search/?api=1&query=$alertLatitude,$alertLongtitude"

            smsManager.sendTextMessage(guardian.phoneNumber, null, alert, null, null)
            Log.d(USER_MANAGER, alert)
        }
    }

    fun getUserLocation(
        activity: Activity,
        context: Context,
        onLocationResult: (location: MyLatLng) -> Unit
    ) {
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                singleLocationProvider =
                    LocationServices.getFusedLocationProviderClient(context)
                val singleLocationRequest = LocationRequest.create().apply {
                    numUpdates = 1
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                singleLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)

                        //if null return
                        locationResult ?: return

                        val location = locationResult.locations[locationResult.locations.size - 1]
                        currentUser.location =
                            MyLatLng(
                                location.latitude,
                                location.longitude
                            )

                        Log.d(
                            USER_MANAGER,
                            "Get one off user location: ${currentUser.location?.latitude} & ${currentUser.location?.longitude}"
                        )

                        onLocationResult.invoke(
                            MyLatLng(
                                location.latitude,
                                location.longitude
                            )
                        )

                        singleLocationProvider.removeLocationUpdates(
                            singleLocationCallback
                        )
                    }
                }
                singleLocationProvider.requestLocationUpdates(
                    singleLocationRequest,
                    singleLocationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            requestPermissions(activity)
        }
    }

    fun uploadImageToFirebaseStorage(photoUri: Uri, callback: (photoUri: Uri) -> Unit) {
        if (photoUri == null) return

        val filename = UUID.randomUUID().toString()
        val imagesRef = FirebaseStorage.getInstance().getReference("/images/$filename")

        imagesRef.putFile(photoUri)
            .addOnSuccessListener {
                Log.d(USER_MANAGER, "Image uploaded: ${it.metadata?.path}")

                imagesRef.downloadUrl.addOnSuccessListener { url ->
                    callback.invoke(url)
                }
            }
            .addOnFailureListener {
                Log.d(USER_MANAGER, "Failed to upload image")
            }
    }
}