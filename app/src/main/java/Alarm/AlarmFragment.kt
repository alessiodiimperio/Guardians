package Alarm

import Settings.PreferencesSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.google.firebase.firestore.remote.Datastore
import models.Alarm
import models.DataStore
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import java.util.*

const val ALARM_FRAGMENT:String = "ALARM_FRAGMENT"
const val REQUEST_PERMISSION_SMS_FINE_LOCATION : Int = 2
const val ALARMING_FRAGMENT:String = "ALARMING_FRAGMENT"
const val CREATE_PIN_FRAGMENT:String = "CREATE_PIN_FRAGMENT"
const val INSERT_PIN_FRAGMENT:String = "INSERT_PIN_FRAGMENT"
const val COUNTDOWN_ACTIVATION_LENGTH: Long = 6000
const val COUNTDOWN_PIN_LENGTH: Long = 11000
const val TICK_LENGTH: Long = 1000

class AlarmFragment : Fragment() {

    lateinit var alarm:Alarm
    lateinit var triggerBttn:Button
    lateinit var defuseBttn:Button
    lateinit var countDownTextView:TextView
    lateinit var activationProgressCircle:ProgressBar
    lateinit var countDownTimer: CountDownTimer
    var triggerBttnInitialSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarm = Alarm(this)

        hideToolbar()

        triggerBttn = view.findViewById(R.id.alarm_trigger)
        defuseBttn = view.findViewById<Button>(R.id.defuse_button)
        countDownTextView = view.findViewById<TextView>(R.id.counter_text)
        activationProgressCircle = view.findViewById<ProgressBar>(R.id.activatingProgressBar)
        triggerBttnInitialSize = triggerBttn.layoutParams.width

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if (PreferencesSettings.getCode(context!!) == null){
                    Toast.makeText(context, "PIN must first be set in settings", Toast.LENGTH_SHORT).show()
                    return true
                }
                if (activity?.checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || activity?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS, android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_SMS_FINE_LOCATION)
                    return true
                }

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        alarm.stateMachine.transition(Alarm.Event.AlarmButtonPressed)
                    }

                    MotionEvent.ACTION_UP -> {
                        alarm.stateMachine.transition(Alarm.Event.AlarmButtonReleased)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        defuseBttn.setOnClickListener {
            showDefuseFragment()
        }
    }
    fun renderIdle() {

        // - UI components
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        showBottomNav()
        // - Logic
        countDownTimer.cancel()
        removeFragmentByTag(ALARMING_FRAGMENT)

        //Change button color back
        //Change button sizd back
        //Change backgroundcolor back
    }

    fun renderActivating() {

        // - UI Components
        hideBottomNav()

        countDownTextView.visibility = VISIBLE
        activationProgressCircle.visibility = VISIBLE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE

        showCountDown(COUNTDOWN_ACTIVATION_LENGTH)

        //Animate progressbar pre trigger activation
        //Animate background color to red
        //Animate color change of trigger button
    }

    fun renderActivated() {
        hideBottomNav()

        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        upSizeTriggerBttn()

        //ScaleTriggerToScreenSize
        //Initiallize button pulsating animation
    }

    fun renderDefusing() {
        hideBottomNav()

        countDownTextView.visibility = VISIBLE
        triggerBttn.visibility = GONE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE
        showCountDown(COUNTDOWN_PIN_LENGTH)
        downSizeTriggerBttn()
        showDefuseFragment()

        //Show CountDownTimer 10 seconds
        //Deactivate physical buttons to deter closing app to avoid alarm or switching off phone
    }

    fun renderAlarming() {
        hideBottomNav()

        triggerBttn.visibility = GONE
        countDownTextView.visibility = GONE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE
        removeFragmentByTag(INSERT_PIN_FRAGMENT)
        showAlarmingFragment()
        alertContacts()
        Toast.makeText(context,"Alarm is active - Notifying Guardians", Toast.LENGTH_LONG).show()

        //Flash screen at highest brightness between RED and White to attract attention
        //Play Siren att highest possible volume.
        //Deactivate physical buttons to deter closing app / Switching phone off
    }

    fun renderAlarmingDefusable() {
        hideBottomNav()
        triggerBttn.visibility = GONE
        countDownTextView.visibility = GONE
        defuseBttn.visibility = VISIBLE
        activationProgressCircle.visibility = GONE
    }

    private fun showCountDown(countLength: Long) {
        countDownTimer = object : CountDownTimer(countLength, TICK_LENGTH) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTextView.text = (millisUntilFinished / 1000).toString()
                activationProgressCircle.progress =
                    (millisUntilFinished / countLength * 100).toInt()
            }
            override fun onFinish() {
                countDownTimer.cancel()
            }
        }.start()
    }

    private fun upSizeTriggerBttn() {
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        triggerBttn.layoutParams = params
    }

    private fun downSizeTriggerBttn() {
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = triggerBttnInitialSize
        params.height = triggerBttnInitialSize
        triggerBttn.layoutParams = params
    }
    private fun hideToolbar() {
        (activity as MainActivity).toolbar.visibility = GONE
    }
    private fun hideBottomNav(){
        (activity as MainActivity).bottomNav.visibility = GONE
    }
    private fun showBottomNav(){
        (activity as MainActivity).bottomNav.visibility = VISIBLE
    }

    private val mLoginListener: PFLockScreenFragment.OnPFLockScreenLoginListener = object :
        PFLockScreenFragment.OnPFLockScreenLoginListener {
        override fun onCodeInputSuccessful() {
            Toast.makeText(context,"Alarm Defused", Toast.LENGTH_SHORT).show()
            removeFragmentByTag(INSERT_PIN_FRAGMENT)
            alarm.transition(Alarm.Event.AlarmDefused)
        }
        override fun onPinLoginFailed() {
            Toast.makeText(context, "Wrong PIN", Toast.LENGTH_SHORT).show()
        }

        override fun onFingerprintSuccessful() {
            TODO("Not yet implemented")
        }

        override fun onFingerprintLoginFailed() {
            TODO("Not yet implemented")
        }
    }

    private fun showDefuseFragment() {
        defuseBttn.visibility = GONE
        val builder =
            PFFLockScreenConfiguration.Builder(context)
                .setTitle("Insert PIN to defuse Alarm")
                .setCodeLength(4)
                .setClearCodeOnError(true)
                .setUseFingerprint(false)

        val fragment = PFLockScreenFragment()

        builder.setMode(PFFLockScreenConfiguration.MODE_AUTH)
        val PIN = PreferencesSettings.getCode(context!!)
        if(PIN != null){
            fragment.setEncodedPinCode(PIN)
            fragment.setLoginListener(mLoginListener)
        }
        fragment.setConfiguration(builder.build())
        parentFragmentManager.beginTransaction()
            .add(R.id.alarm_fragment_background_layout, fragment, INSERT_PIN_FRAGMENT).commit()
    }
    private fun removeFragmentByTag(tag:String){
        val fragment = parentFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.remove(fragment).commit()
        }
    }
    fun showAlarmingFragment(){
        val fragment = AlarmingFragment()
        parentFragmentManager.beginTransaction().replace(R.id.alarm_fragment_background_layout, fragment, ALARMING_FRAGMENT).commit()
    }
    fun alertContacts(){
        if(DataStore.currentUser.location.size < 1){
            return
        }

        val alertLocation = DataStore.currentUser.location.get(DataStore.currentUser.location.lastIndex)
        val alertLatitude = alertLocation.latitude
        val alertLongtitude = alertLocation.longitude

        val smsManager = SmsManager.getDefault()

        DataStore.currentUser.guardians.forEach {guardian->
            val currentUserName = DataStore.currentUser.name
            val alert = "$currentUserName's safety alarm has been triggered at the following location: https://www.google.com/maps/search/?api=1&query=$alertLatitude,$alertLongtitude"

            smsManager.sendTextMessage(guardian.mobilNR, null, alert, null,null)
            Log.d(ALARM_FRAGMENT, alert)

        }
    }
}