package net.komunan.komunantw.repository.entity.ext

import androidx.room.ColumnInfo
import net.komunan.komunantw.common.Diffable
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.TweetSource

class TweetSourceExt: TweetSource(), Diffable {
    @ColumnInfo(name = "source_ids") var sourceIdsStr: String = ""

    fun sourceIds(): List<Long> {
        return sourceIdsStr.split(',').map(String::toLong)
    }

    fun credentials(): List<Credential> {
        val sources = Source.dao.find(sourceIds())
        val accountIds = sources.map(Source::accountId).distinct()
        return accountIds.map { Credential.dao.findByAccountId(it).first() }
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is TweetSourceExt
                && this.tweetId == other.tweetId
                && this.isMissing == other.isMissing
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return isTheSame(other)
    }
}
