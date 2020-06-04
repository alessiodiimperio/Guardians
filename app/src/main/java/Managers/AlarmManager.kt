package Managers

import Alarm.AlertMapsFragment
import Alarm.MAPS_FRAGMENT
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import models.AlarmObject
import models.MyLatLng
import se.diimperio.guardians.ALERT_NOTIFICATION_CHANNEL
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import se.diimperio.guardians.RequestCodes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object AlarmManager {

    val ALARM_MANAGER: String = "ALARM_MANAGER"

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val alarmRef = db.collection("alarms")
    val alarmLocations: MutableList<MyLatLng> = mutableListOf()

    fun setupAlarmListener(activity: Activity, context: Context) {

        // Listen on firebase if alarm object is uploaded to server.
        db.collection("alarms").addSnapshotListener { snapshot, error ->

            //Clear local alarm array
            alarmLocations.clear()

            //Get all alarm objects and filter out by distance and uid - notify self if alarm is valid for current user.
            if (snapshot != null && snapshot.documents.size > 0) {
                UserManager.getUserLocation(
                    activity,
                    context
                ) { userLocation ->
                    snapshot.documents.forEach { document ->
                        if (document != null) {
                            val alarmObject =
                                document.toObject(AlarmObject::class.java)

                            if (alarmObject?.location != null && alarmObject.uid != null && alarmObject.uid != auth.uid) {
                                if (distanceBetween(
                                        userLocation,
                                        alarmObject.location
                                    ) < 1500 || alarmObject.guardianUids.contains(UserManager.currentUser.uid)
                                ) {
                                    //Alarm is valid for user. Add to array and notify user
                                    alarmLocations.add(
                                        alarmObject.location
                                    )
                                    sendNotification(
                                        activity,
                                        context
                                    )
                                }
                            }
                        }
                    }
                    //Refresh necessary UI components
                    (activity as MainActivity).checkForActiveAlerts()
                    refreshMap(activity)
                }
            } else {
                (activity as MainActivity).checkForActiveAlerts()
                refreshMap(activity)
            }
        }
    }

    fun uploadAlarmObjectToServer() {
        val uid = UserManager.currentUser.uid
        val location = UserManager.currentUser.location
        if (uid == null || location == null) {
            return
        }
        val time = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timestamp = time.format(formatter)
        val guardians = mutableListOf<String>()

        //Add guardian uids to alarmobject. If they have the app they will be notified regardless of distance between locations
        UserManager.currentUser.guardians.forEach { guardian ->
            if (guardian.uid != null) {
                guardians.add(guardian.uid.toString())
            }
        }

        val alarmObject = AlarmObject(uid, location, timestamp, guardians)
        alarmRef.document("$uid").set(alarmObject).addOnSuccessListener {
            Log.d(ALARM_MANAGER, "Uploaded alarm successfully")
        }.addOnFailureListener { error ->
            Log.d(ALARM_MANAGER, "Alarm Upload Error: ${error.message}")
        }
    }

    fun removeAlarmObjectFromServer() {
        db.collection("alarms").document("${auth.uid}").delete()
            .addOnSuccessListener {
                Log.d(ALARM_MANAGER, "Alarm Object removed from Firebase")
            }.addOnFailureListener { error ->
                Log.d(ALARM_MANAGER, "Error: $error")
            }
    }

    fun activeAlertsExists(): Boolean {
        return alarmLocations.size > 0
    }

    fun distanceBetween( //Measure distance between two latlng positions Haversine formula
        userLocation: MyLatLng,
        alarmLocation: MyLatLng
    ): Int {

        val lat1 = userLocation.latitude!!
        val long1 = userLocation.longitude!!
        val lat2 = alarmLocation.latitude!!
        val long2 = alarmLocation.longitude!!

        val radius = 6371000 // Radius of the earth in meters
        val phiOne = lat1 * Math.PI / 180
        val phiTwo = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (long2 - long1) * Math.PI / 180

        val a =
            Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                    Math.cos(phiOne) * Math.cos(phiTwo) *
                    Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        ;
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val d = radius * c; // Distance in meters


        Log.d(
            ALARM_MANAGER,
            "Distance between User: ($lat1, $long1) and Alarm: ($lat2,$long2) is $d meters"
        )
        return d.toInt() //Return distance in meters
    }

    fun sendNotification(activity: Activity, context: Context) {

        //Create clickable notification to open mainactivity. Put extra boolean to flag open maps fragment
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_SINGLE_TOP
        intent.flags = FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("load_maps_fragment", true)

        val pendingIntent =
            PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val customView = RemoteViews(activity.packageName, R.layout.alert_notification)
        customView.setImageViewResource(R.id.alert_notification_icon, R.drawable.ic_alert_on)
        customView.setImageViewResource(
            R.id.alert_notification_chevron,
            R.drawable.ic_chevron_right
        )

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context, ALERT_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_location_48)
            .setCustomContentView(customView)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(RequestCodes.ALERT_NOTIFICATION, notification)
    }

    fun refreshMap(activity:Activity){
        //On alarm change redraw map with locations.
        val fragment = (activity as MainActivity).supportFragmentManager.findFragmentByTag(
            MAPS_FRAGMENT)
        if(fragment != null){
            Log.d(ALARM_MANAGER, "calling refresh map")
            val alertMapsFragment = fragment as AlertMapsFragment
            alertMapsFragment.refreshMap()
        }
    }
}
