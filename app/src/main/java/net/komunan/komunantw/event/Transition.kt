package net.komunan.komunantw.event

import org.greenrobot.eventbus.EventBus

class Transition private constructor(val target: Target, val id: Long) {
    companion object {
        @JvmStatic
        fun execute(target: Target, id: Long = 0) {
            EventBus.getDefault().post(Transition(target, id))
        }
    }

    enum class Target {
        HOME,
        ACCOUNTS,
        AUTH,
        TIMELINES,
        TIMELINE_EDIT,
        SOURCES,
        BACK,
    }

    override fun toString(): String {
        return "Transition: { target=$target, id=$id }"
    }
}
