package Alarm

import Settings.PreferencesSettings
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import models.Alarm
import models.AlarmManager
import models.UserManager
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

const val ALARM_FRAGMENT: String = "ALARM_FRAGMENT"
const val REQUEST_PERMISSION_SMS_FINE_LOCATION: Int = 2
const val ALARMING_FRAGMENT: String = "ALARMING_FRAGMENT"
const val CREATE_PIN_FRAGMENT: String = "CREATE_PIN_FRAGMENT"
const val INSERT_PIN_FRAGMENT: String = "INSERT_PIN_FRAGMENT"
const val COUNTDOWN_ACTIVATION_LENGTH: Long = 6000
const val COUNTDOWN_PIN_LENGTH: Long = 11000
const val TICK_LENGTH: Long = 1000

class AlarmFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var alarm: Alarm
    lateinit var triggerBttn: MaterialButton
    lateinit var defuseBttn: Button
    lateinit var countDownTextView: TextView
    lateinit var activationProgressCircle: ProgressBar
    lateinit var countDownTimer: CountDownTimer
    var triggerBttnInitialSize = 0
    var triggerButtonRadius = 0

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Pass fragment view to Alarm StateMachine to control event flow
        alarm = Alarm(this)

        hideToolbar()

        mainActivity = activity as MainActivity
        triggerBttn = view.findViewById(R.id.alarm_trigger)
        defuseBttn = view.findViewById<Button>(R.id.defuse_button)
        countDownTextView = view.findViewById<TextView>(R.id.counter_text)
        activationProgressCircle = view.findViewById<ProgressBar>(R.id.activatingProgressBar)
        triggerBttnInitialSize = triggerBttn.layoutParams.width
        triggerButtonRadius = triggerBttn.cornerRadius

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if (PreferencesSettings.getCode(context!!) == null) {
                    Toast.makeText(context, "PIN must first be set in settings", Toast.LENGTH_SHORT)
                        .show()
                    return true
                }
                if (activity?.checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || activity?.checkSelfPermission(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ), REQUEST_PERMISSION_SMS_FINE_LOCATION
                    )
                    return true
                }

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mainActivity.activeAlertBttn.visibility = GONE
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
        if(AlarmManager.activeAlertsExists()) {
            mainActivity.activeAlertBttn.visibility = VISIBLE
        } else {
            mainActivity.activeAlertBttn.visibility = GONE
        }
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        triggerBttn.cornerRadius = triggerButtonRadius
        showBottomNav()

        // - Logic
        countDownTimer.cancel()
        removeFragmentByTag(ALARMING_FRAGMENT)
        UserManager.stopUserLocationUpdates()

        //Change button color back
        //Change button sizd back
        //Change backgroundcolor back
    }

    fun renderActivating() {

        // - UI Components
        countDownTextView.visibility = VISIBLE
        activationProgressCircle.visibility = VISIBLE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE

        showCountDown(COUNTDOWN_ACTIVATION_LENGTH)
        UserManager.startUsersLocationUpdates(activity!!,context!!)

        //Animate background color to red
        //Animate color change of trigger button
    }

    fun renderActivated() {
        hideBottomNav()
        triggerBttn.cornerRadius = 0
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
        UserManager.notifyGuardians()
        Toast.makeText(context, "Alarm is active - Notifying Guardians", Toast.LENGTH_LONG).show()

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

    private fun hideBottomNav() {
        (activity as MainActivity).bottomNav.visibility = GONE
    }

    private fun showBottomNav() {
        (activity as MainActivity).bottomNav.visibility = VISIBLE
    }

    private val mLoginListener: PFLockScreenFragment.OnPFLockScreenLoginListener = object :
        PFLockScreenFragment.OnPFLockScreenLoginListener {
        override fun onCodeInputSuccessful() {
            Toast.makeText(context, "Alarm Defused", Toast.LENGTH_SHORT).show()
            removeFragmentByTag(INSERT_PIN_FRAGMENT)
            alarm.transition(Alarm.Event.AlarmDefused)
        }

        override fun onPinLoginFailed() {
            Toast.makeText(context, "Wrong PIN", Toast.LENGTH_SHORT).show()
        }

        override fun onFingerprintSuccessful() {
            //Unused required override
        }

        override fun onFingerprintLoginFailed() {
            //Unused required override
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
        if (PIN != null) {
            fragment.setEncodedPinCode(PIN)
            fragment.setLoginListener(mLoginListener)
        }
        fragment.setConfiguration(builder.build())
        parentFragmentManager.beginTransaction()
            .add(R.id.alarm_fragment_background_layout, fragment, INSERT_PIN_FRAGMENT).commit()
    }

    private fun removeFragmentByTag(tag: String) {
        val fragment = parentFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.remove(fragment).commit()
        }
        if(tag == ALARMING_FRAGMENT){
            AlarmManager.removeAlarmObjectFromServer()
        }
    }

    fun showAlarmingFragment() {
        val fragment = AlarmingFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.alarm_fragment_background_layout, fragment, ALARMING_FRAGMENT).commit()
    }
}
