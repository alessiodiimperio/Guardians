package Alarm

import Settings.PreferencesSettings
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.View.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_alarm.*
import models.ALARM_MANAGER
import models.Alarm
import models.AlarmManager
import models.UserManager
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import java.security.Permissions
import java.util.zip.Inflater


const val ALARM_FRAGMENT: String = "ALARM_FRAGMENT"
const val REQUEST_PERMISSION_ALARMING: Int = 112
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
    lateinit var background: View
    lateinit var backgroundAnimation: TransitionDrawable
    lateinit var triggerButtonAnimation: TransitionDrawable
    var canOverlay = false

    var triggerBttnInitialSize = 0
    var triggerButtonRadius = 0
    var testMode = false

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

        //Pass fragment view to Alarm StateMachine to control ui changes on states
        alarm = Alarm(this)

        //Initiallizing components
        mainActivity = activity as MainActivity
        triggerBttn = view.findViewById(R.id.alarm_trigger)
        defuseBttn = view.findViewById<Button>(R.id.defuse_button)
        countDownTextView = view.findViewById<TextView>(R.id.counter_text)
        activationProgressCircle = view.findViewById<ProgressBar>(R.id.activatingProgressBar)
        triggerBttnInitialSize =
            triggerBttn.layoutParams.width //save button initial size to restore size on return to idle state
        triggerButtonRadius =
            triggerBttn.cornerRadius // save initial radius to restore circular shape on return to idle
        background = alarm_fragment_background_layout


        hideActionBar() //Cleaner look without actionbar in this fragment

        //check if is in Testmode
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        testMode = sharedPreferences.getBoolean("testmode", false)
        if (testMode) {
            setupTestMode()
        }

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                Log.d(ALARM_MANAGER, "Pin exists: ${PINExists()}")

                if (!PINExists()) {
                    Toast.makeText(context, "PIN must first be set in settings", Toast.LENGTH_SHORT)
                        .show()
                    return true
                }


                val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.SEND_SMS)

                if (!hasPermissions(context!!, permissions)) {
                    ActivityCompat.requestPermissions(mainActivity,permissions,
                        REQUEST_PERMISSION_ALARMING)
                    return true
                }
                /*
                if (!Settings.canDrawOverlays(context)) {
                    getOverlayPermission()
                }
                if(!canOverlay){
                    return true
                }
                 */

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

    fun renderIdle() { //Render UI to Idle State
        // - UI components
        if (AlarmManager.activeAlertsExists()) {
            mainActivity.activeAlertBttn.visibility = VISIBLE
        } else {
            mainActivity.activeAlertBttn.visibility = GONE
        }
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        showBottomNav()

        if (testMode) {
            triggerBttn.setBackgroundColor(resources.getColor(R.color.colorGreenBlueAccent))
        } else {
            triggerBttn.setBackgroundColor(resources.getColor(R.color.colorPurpleAccent))
        }
        backgroundAnimation.reverseTransition(500)
        //triggerButtonAnimation.reverseTransition(500)

        // - Logic
        countDownTimer.cancel()
        removeFragmentByTag(ALARMING_FRAGMENT)
        UserManager.stopUserLocationUpdates()

    }

    fun renderActivating() {
        // - UI Components
        countDownTextView.visibility = VISIBLE
        activationProgressCircle.visibility = VISIBLE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE

        //Logic
        showCountDown(COUNTDOWN_ACTIVATION_LENGTH)
        UserManager.startUsersLocationUpdates(activity!!, context!!)

        //Animations
        if (testMode) {
            backgroundAnimation =
                resources.getDrawable(R.drawable.background_activating_testmode_animation) as TransitionDrawable
            triggerBttn.setBackgroundColor(resources.getColor(R.color.colorGreenBlueAccent))
            //triggerButtonAnimation = resources.getDrawable(R.drawable.trigger_button_activating_testmode_animation) as TransitionDrawable
        } else {
            backgroundAnimation =
                resources.getDrawable(R.drawable.background_activating_animation) as TransitionDrawable
            triggerBttn.setBackgroundColor(resources.getColor(R.color.colorAlertRed))
            //triggerButtonAnimation = resources.getDrawable(R.drawable.trigger_button_activating_animation) as TransitionDrawable
        }

        background.background = backgroundAnimation
        backgroundAnimation.startTransition(5000)

        //Trigger animations not working atm
        //triggerBttn.background = triggerButtonAnimation
        //triggerButtonAnimation.startTransition(5000)


    }

    fun renderActivated() {
        hideBottomNav()
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        upSizeTriggerBttn()

        //Animations not working atm
        //triggerButtonAnimation = resources.getDrawable(R.drawable.trigger_button_activated_animation) as TransitionDrawable
        //triggerBttn.background = triggerButtonAnimation
        //triggerButtonAnimation.startTransition(0)
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
        UserManager.notifyGuardians(testMode)
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
        triggerBttn.cornerRadius = 0
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        triggerBttn.layoutParams = params
    }

    private fun downSizeTriggerBttn() {
        triggerBttn.cornerRadius = triggerButtonRadius
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = triggerBttnInitialSize
        params.height = triggerBttnInitialSize
        triggerBttn.layoutParams = params
    }

    private fun hideActionBar() {
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
        if (tag == ALARMING_FRAGMENT) {
            AlarmManager.removeAlarmObjectFromServer()
        }
    }

    fun showAlarmingFragment() {
        val windowManager: WindowManager =
            context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        val fragment = AlarmingFragment()
            parentFragmentManager.beginTransaction().replace(R.id.alarm_fragment_background_layout, fragment, ALARMING_FRAGMENT).commit()

        //windowManager.addView(activity?.findViewById(R.id.alarm_fragment_background_layout),layoutParams)

    }

    fun setupTestMode() {
        triggerBttn.setText("TestMode")
        triggerBttn.setBackgroundColor(resources.getColor(R.color.colorGreenBlueAccent))
        activationProgressCircle.indeterminateTintList =
            ColorStateList.valueOf(resources.getColor(R.color.colorGreenBlueAccent));
    }

    fun PINExists() = PreferencesSettings.getCode(context!!) != null && PreferencesSettings.getCode(context!!) != ""

    fun hasPermissions(context:Context, permissions:Array<String>):Boolean {
        var hasPermission = true
        permissions.forEach { permission->
            hasPermission = (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
        }
        Log.d(ALARM_FRAGMENT, "hasPermission: $hasPermission")
        return hasPermission
    }
/*  OVERLAY PERMISSIONS ON HOLD----
    private fun getOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity?.packageName)
        )
        startActivityForResult(intent, 554)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 554 && resultCode == PackageManager.PERMISSION_GRANTED)
        canOverlay = true
    }

 */
}
