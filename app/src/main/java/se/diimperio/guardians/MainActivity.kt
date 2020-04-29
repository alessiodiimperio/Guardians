package se.diimperio.guardians

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.solver.GoalRow

const val COUNTDOWN_ACTIVATION_LENGTH:Long = 6000
const val COUNTDOWN_PIN_LENGTH:Long = 10000
const val TICK_LENGTH:Long = 1000

class MainActivity() : AppCompatActivity() {

    lateinit var triggerBttn: Button
    lateinit var defuseBttn:Button
    lateinit var countDownTextView:TextView
    lateinit var activationProgressCircle:ProgressBar
    lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countDownTextView = findViewById<TextView>(R.id.counter_text)
        activationProgressCircle = findViewById<ProgressBar>(R.id.activatingProgressBar)
        triggerBttn = findViewById(R.id.alarm_trigger)
        defuseBttn = findViewById<Button>(R.id.defuse_button)

        val alarm = Alarm(this)

        //Use button to determine transitions in state
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

    fun renderIdle(){

        // - UI components
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE
        menuBttn.visibility = VISIBLE

        // - Logic
        countDownTimer.cancel()

        //Change button color back
        //Change button sizd back
        //Change backgroundcolor back
    }
    fun renderActivating(){

        // - UI Components
        countDownTextView.visibility = VISIBLE
        activationProgressCircle.visibility = VISIBLE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE

        showCountDown(COUNTDOWN_ACTIVATION_LENGTH)

        //Animate progressbar pre trigger activation
        //Animate background color to red
        //Animate color change of trigger button
    }
    fun renderActivated(){
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE
        defuseBttn.visibility = GONE
        triggerBttn.visibility = VISIBLE

        //ScaleTriggerToScreenSize
        //Initiallize button pulsating animation
    }

    fun renderDefusing(){
        triggerBttn.visibility = GONE
        countDownTextView.visibility = VISIBLE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE

        showCountDown(COUNTDOWN_PIN_LENGTH)

        //Show fragment with touchpad to enter PIN to defuse
        //Show CountDownTimer 10 seconds
        //Deactivate physical buttons to deter closing app to avoid alarm or switching off phone
    }
    fun renderAlarming(){
        triggerBttn.visibility = GONE
        countDownTextView.visibility = VISIBLE
        defuseBttn.visibility = GONE
        activationProgressCircle.visibility = GONE

        //Flash screen at highest brightness between RED and White to attract attention
        //Play Siren att highest possible volume.
        //Deactivate physical buttons to deter closing app / Switching phone off
    }
    fun renderAlarmingDefusable(){
        triggerBttn.visibility = GONE
        countDownTextView.visibility = GONE
        defuseBttn.visibility = VISIBLE
        activationProgressCircle.visibility = GONE
        //Show defuse button to allow defusing of alarm with pin
    }
    fun showCountDown(length:Long){

        countDownTimer = object : CountDownTimer(length, TICK_LENGTH) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTextView.text = (millisUntilFinished / 1000).toString()
                activationProgressCircle.progress = (1000 / millisUntilFinished * 100).toInt()
            }

            override fun onFinish() {
                countDownTimer.cancel()
            }

        }.start()
    }
}