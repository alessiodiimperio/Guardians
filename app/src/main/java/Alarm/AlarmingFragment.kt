package Alarm

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.cloud.audit.AuditLogOrBuilder
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R


class AlarmingFragment : Fragment() {

    lateinit var player:MediaPlayer
    lateinit var background:View
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAc80AMG4:APA91bENLXE7u0G6S3HW1Y4mLUAN0_xswrl4e9jCKreNbzSSNWjLkaepC_7CBj0m96AzksEl-D6wl69G-NwSyYneBsps82LntGS1zdh8EOB3qPwmxaY_NT9gTYfUmusHDBR0mgrETJxH"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(activity!!.applicationContext)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarming, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(context, "Alarm is active - Notifying Guardians", Toast.LENGTH_LONG).show()

        //Get preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val testMode = sharedPreferences.getBoolean("testmode", false)

        //Init mediaplayer
        player = MediaPlayer.create(activity?.applicationContext, R.raw.alarm)

        //Init background animation
        background = view.findViewById(R.id.alarming_fragment_background)
        val animationDrawable = background.background as AnimationDrawable
        animationDrawable.isOneShot = false
        animationDrawable.setEnterFadeDuration(10)
        animationDrawable.setExitFadeDuration(400)
        animationDrawable.start()

        //If testmode dont make noise
        if(!testMode) {
            playSound()
        } else {
            Toast.makeText(context, "Loud noise ringing when not TESTMODE", Toast.LENGTH_LONG).show()
        }
    }

    private fun playSound(){
        val audioManager = activity?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        player.isLooping = true
        player.start();
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    private fun alertContacts(){

/* FIRESTORE Cloud messaging push notification. On hold.
        val topic = "/topics/Alert" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", "Guardian Alert")
            notifcationBody.put("message", "${UserManager.currentUser.name}'s safety alarm triggered!")   //Enter your notification message
            notification.put("to", topic)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }
        sendNotification(notification)
    }
    private fun sendNotification(notification: JSONObject) {
        Log.d(ALARMING_FRAGMENT, "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.d(ALARMING_FRAGMENT, "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show()
                Log.d(ALARMING_FRAGMENT, "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
 */
    }
}
