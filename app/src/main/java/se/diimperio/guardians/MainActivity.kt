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

class MainActivity() : AppCompatActivity() {

    lateinit var triggerBttn: Button
    lateinit var defuseBttn:Button
    lateinit var countDownTextView:TextView
    lateinit var activationProgressCircle:ProgressBar

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

    fun renderActivating(){

        countDownTextView.visibility = VISIBLE
        activationProgressCircle.visibility = VISIBLE

        val countDown = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTextView.text = (millisUntilFinished / 1000).toString()
                activationProgressCircle.progress = (millisUntilFinished / 100).toInt()
            }

            override fun onFinish() {

            }

        }.start()

        //Animate progressbar pre trigger activation
        //Animate background color to red
        //Animate color change of trigger button
    }
    fun renderIdle(){
        countDownTextView.visibility = GONE
        activationProgressCircle.visibility = GONE

    }
}
