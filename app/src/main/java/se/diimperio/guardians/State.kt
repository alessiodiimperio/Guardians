package se.diimperio.guardians

sealed class State {
    object Idle : State()
    object Activating : State()
    object Activated : State()
    object Defusing : State()
    object Alarming : State()
}
