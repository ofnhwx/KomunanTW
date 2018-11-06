package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.SourceForSelect
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.service.TwitterService

@Suppress("FunctionName")
@Dao
abstract class SourceDao {
    fun findAllAsync() = __findAllAsync()
    fun findByTimelineAsync(timeline: Timeline) = __findByTimelineIdAsync(timeline.id)
    fun findForTimelineEditAsync(timeline: Timeline) = __findForTimelineEditAsync(timeline.id)
    fun find(id: Long) = __find(id)
    fun findEnabled() = __findEnabled()
    fun findByAccount(account: Account) = __findByAccountId(account.id)
    fun findByTimeline(timeline: Timeline) = __findByTimelineId(timeline.id)
    fun countByTimeline(timeline: Timeline) = __countByTimelineId(timeline.id)
    fun save(source: Source) = __save__(source)
    fun save(sources: List<Source>) = __save__(sources)
    fun delete(source: Source) = __delete(source)
    fun delete(sources: List<Source>) = __delete(sources)
    fun updateFetchAt(source: Source) = __updateFetchAt__(source)
    fun updateFor(account: Account) = __updateFor__(account)

    /* ==================== SQL Definitions. ==================== */
    @Query("SELECT source.* FROM source LEFT OUTER JOIN account ON account.id = source.account_id ORDER BY account.name ASC, `order` ASC, label ASC")
    protected abstract fun __findAllAsync(): LiveData<List<Source>>
    @Query("SELECT * FROM source WHERE id in (SELECT source_id FROM timeline_source WHERE timeline_id = :timelineId) ORDER BY id ASC")
    protected abstract fun __findByTimelineIdAsync(timelineId: Long): LiveData<List<Source>>
    @Query("""SELECT s.*, ifnull(ts.timeline_id, 0) AS is_active
FROM source AS s
LEFT OUTER JOIN timeline_source AS ts ON ts.source_id = s.id AND ts.timeline_id = :timelineId
LEFT OUTER JOIN account AS a ON s.account_id = a.id
ORDER BY a.name ASC, s.`order` ASC, s.label ASC""")
    protected abstract fun __findForTimelineEditAsync(timelineId: Long): LiveData<List<SourceForSelect>>
    @Query("SELECT * FROM source WHERE id = :id")
    protected abstract fun __find(id: Long): Source?
    @Query("SELECT * FROM source WHERE EXISTS (SELECT * FROM timeline_source WHERE source_id = source.id) ORDER BY id ASC")
    protected abstract fun __findEnabled(): List<Source>
    @Query("SELECT * FROM source WHERE account_id = :accountId")
    protected abstract fun __findByAccountId(accountId: Long): List<Source>
    @Query("SELECT * FROM source WHERE id IN (SELECT source_id FROM timeline_source WHERE timeline_id = :timelineId)")
    protected abstract fun __findByTimelineId(timelineId: Long): List<Source>
    @Query("SELECT COUNT(*) FROM source WHERE id in (SELECT source_id FROM timeline_source WHERE timeline_id = :timelineId)")
    protected abstract fun __countByTimelineId(timelineId: Long): Int
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun __insert(source: Source): Long
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun __insert(sources: Collection<Source>): List<Long>
    @Update
    protected abstract fun __update(source: Source)
    @Update
    protected abstract fun __update(sources: Collection<Source>)
    @Delete
    protected abstract fun __delete(source: Source)
    @Delete
    protected abstract fun __delete(sources: Collection<Source>)

    /* ==================== Private Functions. ==================== */

    private fun __save__(source: Source): Source {
        if (source.id == 0L) {
            source.id = __insert(source.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            __update(source.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $source" }
        return source
    }

    private fun __save__(sources: Collection<Source>) {
        __update(sources.filter { it.id != 0L }.map { it.apply { updateAt = System.currentTimeMillis() } })
        val insertIds = __insert(sources.filter { it.id == 0L }.map { it.apply { createAt = System.currentTimeMillis() } })
        sources.zip(insertIds).forEach {
            it.first.id = it.second
        }
    }

    private fun __updateFetchAt__(source: Source) {
        source.fetchAt = System.currentTimeMillis()
        __save__(source)
    }

    private fun __updateFor__(account: Account) = transaction {
        val credential = account.credential()
        val twitter = TwitterService.twitter(credential)
        val sources = mutableListOf<Source>()
        val deleted = mutableListOf<Source>()
        val currentSources = account.sources()

        // 標準
        fun processDefaultSources() {
            Source.SourceType.values().filter { it.standard }.forEach { sourceType ->
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
