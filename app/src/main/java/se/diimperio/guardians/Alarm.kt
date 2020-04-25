package se.diimperio.guardians

import android.os.CountDownTimer
import com.tinder.StateMachine
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.schedule

class Alarm() {

    sealed class State {
        object Idle : State()
        object Activating : State()
        object Activated : State()
        object Defusing : State()
        object Alarming : State()
        object AlarmingDefusable : State()
    }

    sealed class Event {
        object TriggerPressed : Event()
        object TriggerActivated : Event()
        object TriggerReleased : Event()
        object AlarmTriggered : Event()
        object AlarmDefused : Event()
        object AlarmDefusable : Event()

    }

    sealed class SideEffect {
        object LogTriggerPressed : SideEffect()
        object LogTriggerActivate : SideEffect()
        object LogTriggerReleased : SideEffect()
        object LogAlarmTriggered : SideEffect()
        object LogAlarmDefused : SideEffect()
        object LogAlarmDefusable : SideEffect()
    }

    val timer = Timer("ACTIVATING")

    var stateMachine = StateMachine.create<State, Event, SideEffect> {
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

        state<State.AlarmingDefusable> {
            on<Event.AlarmDefusable> {
                transitionTo(State.AlarmingDefusable, SideEffect.LogAlarmDefusable)
            }
        }

        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition

            when (validTransition.sideEffect) {
                SideEffect.LogTriggerPressed -> {
                    println("Trigger button has been suppressed")

                    //Animate progressbar pre trigger activation
                    //Animate background color to red
                    //Animate color change of trigger button

                    timer.schedule(5000) {
                        transition(Event.TriggerActivated)
                    }
                }

                SideEffect.LogTriggerActivate -> {
                    println(" Trigger is Active")

                    //ScaleTriggerToScreenSize
                    //Initiallize button pulsating animation
                    //Set up connection to firebase
                    //Update GPS location to firebase every X minutes
                }

                SideEffect.LogTriggerReleased -> {
                    when (it.fromState) {
                        State.Activating -> {
                            println("Trigger has been released - Before being activated")

                            timer.cancel()
                            //Deanimate background to default color
                            //Deanimate progressbar around trigger button
                            //Deanimate button to default color
                        }
                        State.Activated -> {
                            println("Trigger has been released - While being active")

                            //Show Defuse Fragment
                            //Show CountDownTimer
                            //Deactivate physical buttons to deter closing app to avoid alarm
                            //Start firebase countdown to trigger alarm. so in case phone is broken/switched off.. alarm still goes off.

                            timer.schedule(10000) {
                                transition(Event.AlarmTriggered)
                            }
                        }
                    }
                }

                SideEffect.LogAlarmDefused -> {
                    println("Defused")
                    println("Returning to idle state")

                    timer.cancel()
                    //Return to main activity
                    //Animate background back to default color

                }

                SideEffect.LogAlarmTriggered -> {

                    println("Alarm has been triggered")
                    println("Begin notifying ICE contacts")

                    //Set off Alarm to contacts
                    //Flash screen at highest brightness between RED and White to attract attention
                    //Play Siren att highest possible volume.
                    //Deactivate physical buttons to deter closing app / Switching phone off
                    //Update current location to firebase and Alert anyone in near proximity with app. approx 1km radius from alarm trigger location

                    timer.schedule(10000) {
                        transition(Event.AlarmDefusable)
                    }
                }
                SideEffect.LogAlarmDefusable -> {
                    println("Alarm Is now Defusable")

                    //Show Button to display defuse fragment

                }
            }
        }
    }

    fun transition(event: Event) {
        stateMachine.transition(event)
    }

}