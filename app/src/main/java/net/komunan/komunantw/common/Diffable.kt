package net.komunan.komunantw.common

interface Diffable {
    fun isTheSame(other: Diffable): Boolean
    fun isContentsTheSame(other: Diffable): Boolean
}
