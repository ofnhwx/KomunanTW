package net.komunan.komunantw.core.worker

import android.content.Context
import androidx.work.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.Account
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.Timeline
import net.komunan.komunantw.core.service.TwitterService

class UpdateSourcesWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
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

    private val account by lazy { Account.box.get(inputData.getLong(PARAMETER_ACCOUNT_ID, 0))!! }
    private val credential by lazy { account.credentials.first() }

    override fun doWork(): Result {
        ObjectBox.get().runInTx {
            updateStandardSources(account)
            updateListSources(account)
            updateSearchSources(account)
            account.sources.applyChangesToDb()

            if (Timeline.box.isEmpty) {
                val timeline = Timeline(string[R.string.defaults]()).save()
                val source = account.sources.first { it.type == Source.Type.HOME }
                timeline.sources.add(source)
                timeline.sources.applyChangesToDb()
            }
        }
        return Result.success()
    }

    private fun updateStandardSources(account: Account) {
        val listCurrent = account.sources.filter { it.type.standard }
        val listUpdates = Source.Type.values().filter { it.standard }.map { Source(account, it) }
        updateSourcesInternal(account, listCurrent, listUpdates) { source1, source2 -> source1.type == source2.type }
    }

    private fun updateListSources(account: Account) {
        val twitter = TwitterService.twitter(credential)
        val listCurrent = account.sources.filter { it.type == Source.Type.LIST }
        val listUpdates = twitter.getUserLists(account.id).mapTo(mutableListOf()) { Source(account, it) }
        updateSourcesInternal(account, listCurrent, listUpdates) { source1, source2 -> source1.listId == source2.listId }
    }

    private fun updateSearchSources(account: Account) {
        for (source in account.sources.filter { it.type == Source.Type.SEARCH }) {
            source.apply {
                accountName = account.name
                ordinal = Source.Type.SEARCH.ordinal
            }
        }
    }

    private fun updateSourcesInternal(account: Account, listCurrent: List<Source>, listUpdates: List<Source>, isSame: (Source, Source) -> Boolean) {
        // 追加
        listUpdates.forEach { update ->
            if (listCurrent.none { current -> isSame(current, update) }) {
                account.sources.add(update.save())
            }
        }
        // 更新・削除
        for (current in listCurrent) {
            val updates = listUpdates.firstOrNull { isSame(it, current) }
            if (updates == null) {
                account.sources.remove(current)
            } else {
                current.apply {
                    accountName = account.name
                    ordinal = updates.ordinal
                }
            }
        }
    }
}
