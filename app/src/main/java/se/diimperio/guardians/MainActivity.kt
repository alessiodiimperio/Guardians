package se.diimperio.guardians

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.tinder.StateMachine
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    lateinit var triggerBttn: Button
    lateinit var defuseBttn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val alarm = Alarm()

        //Use button to determine transitions in state
        triggerBttn = findViewById(R.id.alarm_trigger)
        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        alarm.stateMachine.transition(Alarm.Event.TriggerPressed)
                    }

                    MotionEvent.ACTION_UP -> {
                        alarm.stateMachine.transition(Alarm.Event.TriggerReleased)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        defuseBttn = findViewById<Button>(R.id.defuse_button)
        defuseBttn.setOnClickListener {
            alarm.stateMachine.transition(Alarm.Event.AlarmDefused)
        }

    }
}
