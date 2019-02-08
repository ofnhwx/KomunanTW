package net.komunan.komunantw.common.worker

import android.content.Context
import androidx.work.*
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.common.extension.transaction
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.R

class UpdateSourcesWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    companion object {
        private const val PARAMETER_ACCOUNT_ID = "UpdateSourcesWorker.PARAMETER_ACCOUNT_ID"

        @JvmStatic
        fun request(accountId: Long): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(UpdateSourcesWorker::class.java)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setInputData(Data.Builder().apply {
                        putLong(PARAMETER_ACCOUNT_ID, accountId)
                    }.build()).build()
        }
    }

    private val account by lazy { Account.dao.find(inputData.getLong(PARAMETER_ACCOUNT_ID, 0))!! }
    private val credential by lazy { Credential.dao.findByAccountId(account.id).first() }

    override fun doWork(): Result {
        transaction {
            val sources = Source.dao.findByAccountId(account.id)
            updateStandardSources(sources)
            updateListSources(sources)
            updateSearchSources(sources)

            if (Timeline.dao.count() == 0) {
                val timeline = Timeline(string[R.string.defaults]()).save()
                val source = Source.dao.findByAccountId(account.id).first { Source.Type.valueOf(it.type) == Source.Type.HOME }
                timeline.addSource(source)
            }
        }
        return Result.success()
    }

    private fun updateStandardSources(sources: List<Source>) {
        val listCurrent = sources.filter { Source.Type.valueOf(it.type).standard }
        val listUpdates = Source.Type.values().filter { it.standard }.map { Source(account, it) }
        updateSourcesInternal(listCurrent, listUpdates) { source1, source2 -> source1.type == source2.type }
    }
    private fun updateListSources(sources: List<Source>) {
        val twitter = TwitterService.twitter(credential)
        val listCurrent = sources.filter { Source.Type.valueOf(it.type) == Source.Type.LIST }
        val listUpdates = twitter.getUserLists(account.id).mapTo(mutableListOf()) { Source(account, it) }
        updateSourcesInternal(listCurrent, listUpdates) { source1, source2 -> source1.listId == source2.listId }
    }

    private fun updateSearchSources(sources: List<Source>) {
        for (source in sources.filter { Source.Type.valueOf(it.type) == Source.Type.SEARCH }) {
            source.apply { ordinal = Source.Type.SEARCH.ordinal}.save()
        }
    }

    private fun updateSourcesInternal(listCurrent: List<Source>, listUpdates: List<Source>, isSame: (Source, Source) -> Boolean) {
        // 追加
        listUpdates.filter { updates -> listCurrent.none { current -> isSame(current, updates) } }.forEach { it.save() }
        // 更新・削除
        for (current in listCurrent) {
            val updates = listUpdates.firstOrNull { isSame(it, current) }
            if (updates == null) {
                current.delete()
            } else {
                current.apply { ordinal = updates.ordinal }.save()
            }
        }
    }
}
