package se.diimperio.guardians

sealed class SideEffect {
    object LogBackToIdle : SideEffect()
    object LogTriggerPressed : SideEffect()
    object LogTriggerActivate : SideEffect()
    object LogTriggerReleased : SideEffect()
    object LogAlarmTriggered : SideEffect()
    object LogAlarmDefused : SideEffect()
}