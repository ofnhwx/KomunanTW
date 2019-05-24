package net.komunan.komunantw.core.repository.entity

import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.i
import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import io.objectbox.relation.ToMany
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.ui.common.base.Diffable

@Entity
class Timeline() : Diffable {
    @Id
    var id: Long = 0L
    var name: String = ""
    var position: Int = 0
    var savedAt: Long = 0

    lateinit var sources: ToMany<Source>

    companion object {
        @JvmStatic
        val box: Box<Timeline>
            get() = ObjectBox.get().boxFor(Timeline::class)

        @JvmStatic
        fun query(): QueryBuilder<Timeline> = box.query()

        @JvmStatic
        fun packPosition() {
            val timelines = query().apply {
                order(Timeline_.position)
            }.build().find()
            timelines.forEachIndexed { index, timeline ->
                timeline.position = index
            }
            box.put(timelines)
        }
    }

    constructor(name: String) : this() {
        this.name = name
    }

    val displaySourceCount: String
        get() = string[R.string.fragment_timeline_list_source_count](sources.count().toString())

    fun moveTo(position: Int) {
        i { "Timeline(${this.id}): move={ from=${this.position}, to=$position }" }
        when {
            // 前に移動
            position < this.position -> ObjectBox.get().runInTx {
                val targets = Timeline.query().apply {
                    between(Timeline_.position, position.toLong(), this@Timeline.position - 1L)
                }.build().find()
                for (target in targets) {
                    d { "> Timeline(${target.id}): move={ from=${target.position}, to=${target.position + 1} }" }
                    target.position = target.position + 1
                    Timeline.box.put(target)
                }
                Timeline.box.put(this.apply {
                    this.position = position
                })
            }
            // 後に移動
            position > this.position -> ObjectBox.get().runInTx {
                val targets = Timeline.query().apply {
                    between(Timeline_.position, this@Timeline.position + 1L, position.toLong())
                }.build().find()
                for (target in targets) {
                    d { "> Timeline(${target.id}): move={ from=${target.position}, to=${target.position - 1} }" }
                    target.position = target.position - 1
                    Timeline.box.put(target)
                }
                Timeline.box.put(this.apply {
                    this.position = position
                })
            }
        }
    }

    fun save(): Timeline {
        box.put(apply {
            if (id == 0L) {
                position = Timeline.box.count().toInt()
            }
            savedAt = System.currentTimeMillis()
        })
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is Timeline
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun isTheSame(other: Diffable): Boolean {
        return this == other
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return false
    }
}
