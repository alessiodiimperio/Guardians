package models

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.media.RingtoneManager.*
import android.util.Log
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import se.diimperio.guardians.R

class MyFirebaseMessagingService : FirebaseMessagingService(){
    override fun onNewToken(newToken: String) {
        Log.d("TOKEN", newToken)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
    }

}