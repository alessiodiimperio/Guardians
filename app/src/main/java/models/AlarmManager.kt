package models

import android.app.Activity
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import se.diimperio.guardians.MainActivity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

const val ALARM_MANAGER: String = "ALARM_MANAGER"

object AlarmManager {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val alarmRef = db.collection("alarms")
    val alarmLocations: MutableList<MyLatLng> = mutableListOf()

    fun setupAlarmListener(activity: Activity?){
        // Listen on firebase if alarm object is uploaded to server.
        db.collection("alarms").addSnapshotListener { snapshot, error ->
            Log.d(ALARM_MANAGER, "Error: $error")

            if (snapshot != null) {
                snapshot.documents.forEach { document->
                    if (document != null) {

                        val alarmObject = document.toObject(AlarmObject::class.java)

                        if (alarmObject?.location != null && alarmObject.uid != null && alarmObject.uid != auth.uid) {
                            alarmLocations.add(alarmObject.location)
                        }
                    }
                }
                (activity as MainActivity).checkForActiveAlerts()
            } else {
                (activity as MainActivity).checkForActiveAlerts()
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
        }.addOnFailureListener { error->
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
}