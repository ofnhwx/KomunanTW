package net.komunan.komunantw.ui.common.base

interface Diffable {
    fun isTheSame(other: Diffable): Boolean
    fun isContentsTheSame(other: Diffable): Boolean
}
