package Alarm

import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.fragment.app.Fragment
import se.diimperio.guardians.Alarm
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

const val COUNTDOWN_ACTIVATION_LENGTH: Long = 6000
const val COUNTDOWN_PIN_LENGTH: Long = 11000
const val TICK_LENGTH: Long = 1000

class AlarmFragment : Fragment() {

    lateinit var triggerBttn:Button
    lateinit var defuseBttn:Button
    lateinit var countDownTextView:TextView
    lateinit var activationProgressCircle:ProgressBar
    lateinit var countDownTimer: CountDownTimer
    var triggerBttnInitialSize = 0
    lateinit var mainActivity: MainActivity

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

        val alarm = Alarm(this)

        triggerBttn = view.findViewById(R.id.alarm_trigger)
        defuseBttn = view.findViewById<Button>(R.id.defuse_button)
        countDownTextView = view.findViewById<TextView>(R.id.counter_text)
        activationProgressCircle = view.findViewById<ProgressBar>(R.id.activatingProgressBar)
        triggerBttnInitialSize = triggerBttn.layoutParams.width

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

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
            alarm.stateMachine.transition(Alarm.Event.AlarmDefused)
        }
    }



    fun renderIdle() {

        // - UI components
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        (activity as MainActivity?)?.showBottomNav()

        // - Logic
        countDownTimer.cancel()

        //Change button color back
        //Change button sizd back
        //Change backgroundcolor back
    }

    fun renderActivating() {

        // - UI Components
        (activity as MainActivity?)?.hideBottomNav()

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
        (activity as MainActivity?)?.hideBottomNav()

        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        upSizeTriggerBttn()

        //ScaleTriggerToScreenSize
        //Initiallize button pulsating animation
    }

    fun renderDefusing() {
        (activity as MainActivity?)?.hideBottomNav()

        triggerBttn.visibility = GONE
        countDownTextView.visibility = VISIBLE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE

        showCountDown(COUNTDOWN_PIN_LENGTH)
        downSizeTriggerBttn()

        //Show fragment with touchpad to enter PIN to defuse
        //Show CountDownTimer 10 seconds
        //Deactivate physical buttons to deter closing app to avoid alarm or switching off phone
    }

    fun renderAlarming() {
        (activity as MainActivity?)?.hideBottomNav()

        triggerBttn.visibility = GONE
        countDownTextView.visibility = GONE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE

        Toast.makeText(context,"Alarm is activated - Notifying Guardians", Toast.LENGTH_SHORT).show()

        //Flash screen at highest brightness between RED and White to attract attention
        //Play Siren att highest possible volume.
        //Deactivate physical buttons to deter closing app / Switching phone off
    }

    fun renderAlarmingDefusable() {
        (activity as MainActivity?)?.hideBottomNav()

        triggerBttn.visibility = GONE
        countDownTextView.visibility = GONE
        defuseBttn.visibility = VISIBLE
        activationProgressCircle.visibility = GONE

        //on defuse bttn press show defuse fragment
    }

    fun showCountDown(countLength: Long) {
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

    fun upSizeTriggerBttn() {
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        triggerBttn.layoutParams = params


    }

    fun downSizeTriggerBttn() {
        val params: ViewGroup.LayoutParams = triggerBttn.layoutParams
        params.width = triggerBttnInitialSize
        params.height = triggerBttnInitialSize
        triggerBttn.layoutParams = params
    }
}
