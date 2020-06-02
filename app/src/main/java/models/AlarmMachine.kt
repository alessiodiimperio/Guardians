package models

import Alarm.AlarmFragment
import android.os.Handler
import android.util.Log
import com.tinder.StateMachine

const val STATE_TIMER = "STATE_TIMER"

class AlarmMachine(view : AlarmFragment) {

    sealed class State {
        object Idle : State()
        object Activating : State()
        object Activated : State()
        object Defusing : State()
        object Alarming : State()
        object AlarmingDefusable : State()
    }

    sealed class Event {
        object AlarmButtonPressed : Event()
        object AlarmButtonActivated : Event()
        object AlarmButtonReleased : Event()
        object AlarmTriggered : Event()
        object AlarmDefused : Event()
        object AlarmSetDefusable : Event()

    }

    sealed class SideEffect {
        object AlarmButtonWasPressed : SideEffect()
        object AlarmButtonWasActivated : SideEffect()
        object AlarmButtonWasReleased : SideEffect()
        object AlarmHasBeenTriggered : SideEffect()
        object AlarmHasBeenDefused : SideEffect()
        object AlarmHasBeenMadeDefusable : SideEffect()
    }

    var stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.Idle)

        val handler = Handler()
        val transitionToActivatedState = object : Runnable {
            override fun run() {
                transition(Event.AlarmButtonActivated)
            }
        }
        val transitionToAlarmTriggered = object : Runnable {
            override fun run() {
                transition(Event.AlarmTriggered)
            }
        }
        val transitionAlarmToDefusableState = object : Runnable {
            override fun run() {
                transition(Event.AlarmSetDefusable)
            }
        }


        state<State.Idle> {
            on<Event.AlarmButtonPressed> {
                transitionTo(
                    State.Activating,
                    SideEffect.AlarmButtonWasPressed
                )
            }
        }

        state<State.Activating> {
            on<Event.AlarmButtonActivated> {
                transitionTo(
                    State.Activated,
                    SideEffect.AlarmButtonWasActivated
                )
            }

            on<Event.AlarmButtonReleased> {
                transitionTo(
                    State.Idle,
                    SideEffect.AlarmButtonWasReleased
                )
            }
        }

        state<State.Activated> {
            on<Event.AlarmButtonReleased> {
                transitionTo(
                    State.Defusing,
                    SideEffect.AlarmButtonWasReleased
                )
            }
        }

        state<State.Defusing> {
            on<Event.AlarmDefused> {
                transitionTo(
                    State.Idle,
                    SideEffect.AlarmHasBeenDefused
                )
            }
            on<Event.AlarmTriggered> {
                transitionTo(
                    State.Alarming,
                    SideEffect.AlarmHasBeenTriggered
                )
            }
        }

        state<State.Alarming> {
            on<Event.AlarmSetDefusable> {
                transitionTo(
                    State.AlarmingDefusable,
                    SideEffect.AlarmHasBeenMadeDefusable
                )
            }
        }

        state<State.AlarmingDefusable> {
            on<Event.AlarmDefused> {
                transitionTo(
                    State.Idle,
                    SideEffect.AlarmHasBeenDefused
                )
            }
        }

        onTransition {

            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition


            when (validTransition.sideEffect) {

                SideEffect.AlarmButtonWasPressed -> {

                    Log.d("STATE_MACHINE", "AlarmButton Pressed")

                    handler.postDelayed(transitionToActivatedState, 5000)
                    view.renderActivating()

                }

                SideEffect.AlarmButtonWasActivated -> {

                    Log.d("STATE_MACHINE", "AlarmButton trigger is now active")

                    view.renderActivated()

                    //Set up connection to firebase
                    //Update GPS location to firebase every X minutes
                }

                SideEffect.AlarmButtonWasReleased -> {
                    when (it.fromState) {
                        State.Activating -> {
                            Log.d("STATE_MACHINE", "AlarmButton trigger released before activating")

                            view.renderIdle()

                            handler.removeCallbacks(transitionToActivatedState)
                        }

                        State.Activated -> {
                            Log.d("STATE_MACHINE", "Alarm has been activated. 10 seconds to defuse")

                            view.renderDefusing()

                            //Start firebase countdown to trigger alarm. so in case phone is broken/switched off.. alarm still goes off.
                            handler.postDelayed(transitionToAlarmTriggered, 10000)
                        }
                    }
                }

                SideEffect.AlarmHasBeenDefused -> {

                    Log.d("STATE_MACHINE", "Alarm has been defused")
                    handler.removeCallbacks(transitionToAlarmTriggered)

                    view.renderIdle()
                    //Notify firebase Alarm Has been defused
                }

                SideEffect.AlarmHasBeenTriggered -> {
                    Log.d("STATE_MACHINE", "Alarm is now active. Notifying ICE contacts")

                    //Set off Alarm to contacts
                    //Update current location to firebase and Alert anyone in near proximity with app. approx 1km radius from alarm trigger location

                    view.renderAlarming()

                    handler.postDelayed(transitionAlarmToDefusableState, 10000)

                }
                SideEffect.AlarmHasBeenMadeDefusable -> {

                    Log.d("STATE_MACHINE", "Alarm has changed to a defusable state")

                    view.renderAlarmingDefusable()

                }
            }
        }
    }
    fun isActive():Boolean{
        return stateMachine.state == State.Alarming || stateMachine.state == State.AlarmingDefusable
    }
    fun transition(event: Event) {
        stateMachine.transition(event)
    }
}