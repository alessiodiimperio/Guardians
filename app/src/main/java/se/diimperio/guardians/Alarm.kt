package se.diimperio.guardians

import android.os.Handler
import com.tinder.StateMachine

const val STATE_TIMER = "STATE_TIMER"

class Alarm(mainView : MainActivity) {

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
        val runDelayedTransitionToActivatedState = object : Runnable {
            override fun run() {
                transition(Event.AlarmButtonReleased)
                println("in delayed button released")
            }
        }
        val runDelayedTransitionToAlarmTriggered = object : Runnable {
            override fun run() {
                transition(Event.AlarmTriggered)
                println("in delayed trigger alarm")
            }
        }
        val runDelayedTransitionToAlarmSetDefusable = object : Runnable {
            override fun run() {
                transition(Event.AlarmSetDefusable)
                println("in delayed set defusable")
            }
        }


        state<State.Idle> {
            on<Event.AlarmButtonPressed> {
                transitionTo(State.Activating, SideEffect.AlarmButtonWasPressed)
            }
        }

        state<State.Activating> {

            on<Event.AlarmButtonActivated> {
                transitionTo(State.Activated, SideEffect.AlarmButtonWasActivated)
            }

            on<Event.AlarmButtonReleased> {
                transitionTo(State.Idle, SideEffect.AlarmButtonWasReleased)
            }
        }

        state<State.Activated> {
            on<Event.AlarmButtonReleased> {
                transitionTo(State.Defusing, SideEffect.AlarmButtonWasReleased)
            }
        }

        state<State.Defusing> {
            on<Event.AlarmDefused> {
                transitionTo(State.Idle, SideEffect.AlarmHasBeenDefused)
            }
            on<Event.AlarmTriggered> {
                transitionTo(State.Alarming, SideEffect.AlarmHasBeenTriggered)
            }
        }

        state<State.Alarming> {
            on<Event.AlarmDefused> {
                transitionTo(State.Idle, SideEffect.AlarmHasBeenDefused)
            }
        }

        state<State.AlarmingDefusable> {
            on<Event.AlarmSetDefusable> {
                transitionTo(State.AlarmingDefusable, SideEffect.AlarmHasBeenMadeDefusable)
            }
        }

        onTransition {

            val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition


            when (validTransition.sideEffect) {

                SideEffect.AlarmButtonWasPressed -> {
                    println("button pressed")
                    mainView.renderActivating()

                    handler.postDelayed(runDelayedTransitionToActivatedState, 6000)

                }

                SideEffect.AlarmButtonWasActivated -> {
                    println("alarm button activated")

                    //ScaleTriggerToScreenSize
                    //Initiallize button pulsating animation
                    //Set up connection to firebase
                    //Update GPS location to firebase every X minutes
                }

                SideEffect.AlarmButtonWasReleased -> {
                    when (it.fromState) {
                        State.Activating -> {
                            println("alarm button released safely in state :${validTransition.fromState}")

                            handler.removeCallbacks(runDelayedTransitionToActivatedState)
                            mainView.renderIdle()



                            //Deanimate background to default color
                            //Deanimate progressbar around trigger button
                            //Deanimate button to default color
                        }
                        State.Activated -> {
                            println("alarm button triggered entering state:${validTransition.toState}")
                            //Show Defuse Fragment
                            //Show CountDownTimer
                            //Deactivate physical buttons to deter closing app to avoid alarm
                            //Start firebase countdown to trigger alarm. so in case phone is broken/switched off.. alarm still goes off.

                            handler.postDelayed(runDelayedTransitionToAlarmTriggered, 10000)
                        }
                    }
                }

                SideEffect.AlarmHasBeenDefused -> {

                    println("alarm defused safely in state")
                    handler.removeCallbacks(runDelayedTransitionToAlarmTriggered)

                    //Return to main activity
                    //Animate background back to default color

                }

                SideEffect.AlarmHasBeenTriggered -> {
                    println("alarm has been triggered - contact ICE contacts current state :${validTransition.toState}")

                    //Set off Alarm to contacts
                    //Flash screen at highest brightness between RED and White to attract attention
                    //Play Siren att highest possible volume.
                    //Deactivate physical buttons to deter closing app / Switching phone off
                    //Update current location to firebase and Alert anyone in near proximity with app. approx 1km radius from alarm trigger location

                    handler.postDelayed(runDelayedTransitionToAlarmSetDefusable, 10000)
                }
                SideEffect.AlarmHasBeenMadeDefusable -> {

                    println("alarm is still triggered but defusable state is: ${validTransition.toState}")

                    //Show Button to display defuse fragment

                }
            }
        }
    }

    fun transition(event: Event) {
        stateMachine.transition(event)
    }

}