package net.komunan.komunantw.event

import org.greenrobot.eventbus.EventBus

class Transition private constructor(val target: Target) {
    companion object {
        @JvmStatic
        fun execute(target: Target) {
            EventBus.getDefault().post(Transition(target))
        }
    }

    enum class Target {
        HOME,
        ACCOUNTS,
        AUTH,
        TIMELINES,
        SOURCES,
    }

    override fun toString(): String {
        return "Transition: { target=$target }"
    }
}
