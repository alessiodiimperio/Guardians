package se.diimperio.guardians

sealed class Event {
    object TriggerPressed : Event()
    object TriggerActivated : Event()
    object TriggerReleased : Event()
    object AlarmTriggered : Event()
    object AlarmDefused : Event()

}
