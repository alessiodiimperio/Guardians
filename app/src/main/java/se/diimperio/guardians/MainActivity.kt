package se.diimperio.guardians

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.tinder.StateMachine

class MainActivity : AppCompatActivity() {

    lateinit var triggerBttn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stateMachine = StateMachine.create<State, Event, SideEffect> {
            initialState(State.Idle)

            state<State.Idle> {
                on<Event.TriggerPressed> {
                    transitionTo(State.Activating, SideEffect.LogTriggerPressed)
                }
            }

            state<State.Activating> {
                on<Event.TriggerActivated> {
                    transitionTo(State.Activated, SideEffect.LogTriggerActivate)
                }

                on<Event.TriggerReleased> {
                    transitionTo(State.Idle, SideEffect.LogBackToIdle)
                }
            }
            state<State.Activated> {
                on<Event.TriggerReleased> {
                    transitionTo(State.Defusing, SideEffect.LogDefusing)
                }
            }

            state<State.Defusing> {
                on<Event.AlarmDefused> {
                    transitionTo(State.Idle, SideEffect.LogDefusing)
                }
                on<Event.AlarmTriggered> {
                    transitionTo(State.Alarming, SideEffect.LogAlarmTriggered)
                }
            }
            state<State.Alarming> {
                on<Event.AlarmDefused> {
                    transitionTo(State.Idle, SideEffect.LogAlarmDefused)
                }
            }

            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

                when (validTransition.sideEffect) {
                    SideEffect.LogTriggerPressed -> println("Trigger button has been pressed down")
                    SideEffect.LogTriggerActivate -> println(" Trigger is Active")
                    SideEffect.LogTriggerReleased -> println("Trigger has been released")
                    SideEffect.LogDefusing -> println("Defusing initiated")
                    SideEffect.LogAlarmDefused -> println("Defused")
                    SideEffect.LogAlarmTriggered -> println("Alarm Triggered")
                    SideEffect.LogBackToIdle -> println("Returning to idle state")
                }
            }
        }
        triggerBttn = findViewById(R.id.alarm_trigger)

        triggerBttn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                var timePressed:Long = 0

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        stateMachine.transition(Event.TriggerPressed)
                    }
                    MotionEvent.ACTION_UP -> {
                        stateMachine.transition(Event.TriggerReleased)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

    }
}
