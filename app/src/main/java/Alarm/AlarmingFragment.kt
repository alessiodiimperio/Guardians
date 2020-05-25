package Alarm

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import models.UserManager
import org.json.JSONException
import org.json.JSONObject
import se.diimperio.guardians.R

class AlarmingFragment : Fragment() {

    lateinit var animator:ObjectAnimator
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

        animator = ObjectAnimator.ofInt(
            view, "backgroundColor", Color.BLUE, Color.RED, Color.BLUE)
        blinkEffect()

        //Not working ???
        alertContacts()

    }
    fun blinkEffect(){
        animator.duration = 500
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = Animation.INFINITE
        animator.start()
    }
    private fun alertContacts(){

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
    }
}
