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

        val stateMachine = StateMachine.create<State, Event, SideEffect> {
            initialState(State.Idle)

            /************** IDLE **************/
            state<State.Idle> {
                on<Event.TriggerPressed> {
                    transitionTo(State.Activating, SideEffect.LogTriggerPressed)
                }
            }

            /************** ACTIVATING **************/
            state<State.Activating> {
                on<Event.TriggerActivated> {
                    transitionTo(State.Activated, SideEffect.LogTriggerActivate)
                }

                on<Event.TriggerReleased> {
                    transitionTo(State.Idle, SideEffect.LogTriggerReleased)
                }
            }

            /************** ACTIVATED **************/
            state<State.Activated> {
                on<Event.TriggerReleased> {
                    transitionTo(State.Defusing, SideEffect.LogTriggerReleased)
                }
            }

            /************** DEFUSING **************/
            state<State.Defusing> {
                on<Event.AlarmDefused> {
                    transitionTo(State.Idle, SideEffect.LogAlarmDefused)
                }
                on<Event.AlarmTriggered> {
                    transitionTo(State.Alarming, SideEffect.LogAlarmTriggered)
                }
            }

            /************** ALARMING **************/
            state<State.Alarming> {
                on<Event.AlarmDefused> {
                    transitionTo(State.Idle, SideEffect.LogAlarmDefused)
                }
            }

            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

                when (validTransition.sideEffect) {
                    SideEffect.LogTriggerPressed -> {
                        println("Trigger button has been suppressed")
                    }

                    SideEffect.LogTriggerActivate -> {
                        println(" Trigger is Active")
                    }

                    SideEffect.LogTriggerReleased -> {
                        when (it.fromState) {
                            State.Activating -> println("Trigger has been released - Before being activated")
                            State.Activated -> println("Trigger has been released - While being active")
                        }
                    }

                    SideEffect.LogAlarmDefused -> {
                        println("Defused")
                        println("Returning to idle state")
                    }

                    SideEffect.LogAlarmTriggered -> {
                        println("Alarm has been triggered")
                        println("Begin notifying ICE contacts")

                    }

                    SideEffect.LogBackToIdle -> {
                        println("Returning to idle state")
                    }
                }
            }
        }

        //Use button to determine transitions in state
        triggerBttn = findViewById(R.id.alarm_trigger)
        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                var triggerActive = false
                var timer = Timer()
                var countDown: Long = 5000
                var counterDown = 0
                var counterUp = 0

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        stateMachine.transition(Event.TriggerPressed)
                        Timer("Test", false).schedule(5000) {
                            stateMachine.transition(Event.TriggerActivated)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        stateMachine.transition(Event.TriggerReleased)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        defuseBttn = findViewById<Button>(R.id.defuse_button)
        defuseBttn.setOnClickListener {
            stateMachine.transition(Event.AlarmDefused)
        }

    }
}
