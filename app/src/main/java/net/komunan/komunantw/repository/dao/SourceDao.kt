package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.service.TwitterService

@Dao
abstract class SourceDao {
    @Query("SELECT * FROM source WHERE id in (SELECT source_id FROM timeline_source WHERE timeline_id = :timelineId)")
    abstract fun findByTimelineIdAsync(timelineId: Long): LiveData<List<Source>>

    @Query("SELECT * FROM source WHERE id = :id")
    abstract fun find(id: Long): Source

    @Query("SELECT * FROM source WHERE EXISTS (SELECT * FROM timeline_source WHERE source_id = source.id)")
    abstract fun findEnabled(): List<Source>

    @Query("SELECT * FROM source WHERE account_id = :accountId")
    abstract fun findByAccountId(accountId: Long): List<Source>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(source: Source): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(sources: Collection<Source>): List<Long>

    @Update
    abstract fun _update(source: Source)

    @Update
    abstract fun _update(sources: Collection<Source>)

    @Delete
    abstract fun delete(source: Source)

    @Delete
    abstract fun delete(sources: Collection<Source>)

    fun save(source: Source) {
        if (source.id == 0L) {
            source.id = _insert(source.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(source.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $this" }
    }

    fun save(sources: Collection<Source>) {
        _update(sources.filter { it.id != 0L }.map { it.apply { updateAt = System.currentTimeMillis() } })
        val insertIds = _insert(sources.filter { it.id == 0L }.map { it.apply { createAt = System.currentTimeMillis() } })
        sources.zip(insertIds).forEach {
            it.first.id = it.second
        }
    }

    fun updateFetchAt(source: Source) {
        source.fetchAt = System.currentTimeMillis()
        save(source)
    }


    fun update(account: Account) = transaction {
        val credential = account.credential()
        val twitter = TwitterService.twitter(credential)
        val sources = mutableListOf<Source>()
        val deleted = mutableListOf<Source>()
        val currentSources = account.sources()

        // 標準
        fun processDefaultSources() {
            listOf(Source.SourceType.HOME, Source.SourceType.MENTION, Source.SourceType.RETWEET, Source.SourceType.USER).forEach { sourceType ->
                val current = currentSources.firstOrNull { Source.SourceType.valueOf(it.type) == sourceType }
                if (current == null) {
                    sources.add(Source(account, sourceType))
                } else {
                    sources.add(current.apply {
                        order = sourceType.ordinal
                    })
                }
            }
        }

        // リスト
        fun processListSources() {
            val listCurrent = currentSources.filter { current -> Source.SourceType.valueOf(current.type) == Source.SourceType.LIST }
            val listUpdates = twitter.getUserLists(account.id).mapTo(mutableListOf()) { userList ->
                Source(account, Source.SourceType.LIST).apply {
                    label = userList.name
                    listOwner = account.id
                    listId = userList.id
                }
            }
            listCurrent.forEach { current ->
                val update = listUpdates.firstOrNull { current.query == it.query }
                if (update == null) {
                    deleted.add(current)
                } else {
                    listUpdates.remove(update)
                    sources.add(current.apply {
                        order = update.order
                        label = update.label
                    })
                }
            }
            sources.addAll(listUpdates)
        }

        // 検索(そのまま移すだけ)
        fun processSearchSources() {
            currentSources.filter {  Source.SourceType.valueOf(it.type) == Source.SourceType.SEARCH }.forEach { current ->
                sources.add(current.apply {
                    order = Source.SourceType.SEARCH.ordinal
                })
            }
        }

        // 更新
        processDefaultSources()
        processListSources()
        processSearchSources()
        save(sources)
        delete(deleted)
    }
}
