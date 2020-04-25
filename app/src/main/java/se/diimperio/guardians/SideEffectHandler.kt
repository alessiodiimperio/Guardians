package se.diimperio.guardians

interface SideEffectHandler {
    fun onSideEffect(effect:Alarm.SideEffect)
}