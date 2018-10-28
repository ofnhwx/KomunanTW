package net.komunan.komunantw.event

import org.greenrobot.eventbus.EventBus

class Transition private constructor(val target: Target, val isChild: Boolean) {
    companion object {
        @JvmStatic
        fun execute(target: Target) {
            EventBus.getDefault().post(Transition(target, false))
        }

        @JvmStatic
        fun executeChild(target: Target) {
            EventBus.getDefault().post(Transition(target, true))
        }
    }

    enum class Target {
        ACCOUNTS,
        AUTH,
        TIMELINE,
        BACK,
    }
}
