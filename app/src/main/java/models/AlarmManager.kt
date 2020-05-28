package models

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import se.diimperio.guardians.ALERT_NOTIFICATION_CHANNEL
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val ALARM_MANAGER: String = "ALARM_MANAGER"

object AlarmManager {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val alarmRef = db.collection("alarms")
    val alarmLocations: MutableList<MyLatLng> = mutableListOf()

    fun setupAlarmListener(activity: Activity, context: Context) {
        // Listen on firebase if alarm object is uploaded to server.
        db.collection("alarms").addSnapshotListener { snapshot, error ->
            Log.d(ALARM_MANAGER, "Error: $error")

            alarmLocations.clear()
            UserManager.getUserLocation(activity, context) { userLocation ->
                if (snapshot != null) {
                    snapshot.documents.forEach { document ->
                        if (document != null) {

                            val alarmObject = document.toObject(AlarmObject::class.java)

                            if (alarmObject?.location != null && alarmObject.uid != null && alarmObject.uid != auth.uid) {
                                if(distanceBetweenUserAndAlarmLocation(userLocation.latitude!!, userLocation.longitude!!, alarmObject.location.latitude!!, alarmObject.location.longitude!!) < 1500)
                                    alarmLocations.add(alarmObject.location)
                                    sendNotification(activity, context)
                            }
                        }
                    }
                    (activity as MainActivity).checkForActiveAlerts()
                } else {
                    (activity as MainActivity).checkForActiveAlerts()
                }
            }
        }
    }

    fun uploadAlarmObjectToServer() {
        val uid = UserManager.currentUser.uid
        val location = UserManager.currentUser.location
        if (uid == null || location == null) {
            return
        }
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timestamp = current.format(formatter)

        val alarmObject = AlarmObject(uid, location, timestamp)
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

    fun distanceBetweenUserAndAlarmLocation(
        lat1: Double,
        long1: Double,
        lat2: Double,
        long2: Double
    ): Int {

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


        Log.d(ALARM_MANAGER, "Distance between User: ($lat1, $long1) and Alarm: ($lat2,$long2) is $d meters")
        return d.toInt() //Return distance in meters
    }

    fun sendNotification(activity: Activity, context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

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
        notificationManager.notify(1, notification)
    }
}
