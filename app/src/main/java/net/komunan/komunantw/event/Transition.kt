package net.komunan.komunantw.event

import org.greenrobot.eventbus.EventBus

class Transition private constructor(private val target: Target) {
    companion object {
        @JvmStatic
        fun execute(target: Target) {
            EventBus.getDefault().post(Transition(target))
        }
    }

    enum class Target {
        ACCOUNTS,
        AUTH,
        TIMELINE,
    }

    fun getTarget() = target
}
