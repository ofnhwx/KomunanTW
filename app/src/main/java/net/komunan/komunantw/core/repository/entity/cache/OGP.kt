package net.komunan.komunantw.core.repository.entity.cache

import com.github.ajalt.timberkt.w
import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.komunan.komunantw.core.repository.ObjectBox
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

@Entity
class OGP {
    @Id(assignable = true)
    var tweetId: Long = 0L
    var url: String = ""
    var imageUrl: String? = null
    var title: String = ""
    var description: String = ""
    var hasError: Boolean = false
    var expiredAt: Long = 0L

    companion object {
        @JvmStatic
        val box: Box<OGP>
            get() = ObjectBox.get().boxFor(OGP::class.java)

        @JvmStatic
        fun query(): QueryBuilder<OGP> = box.query()

        @JvmStatic
        suspend fun get(tweet: Tweet): OGP {
            val saved = box.query().equal(OGP_.tweetId, tweet.id).build().findFirst()
            if (saved != null && saved.expiredAt < System.currentTimeMillis()) {
                return saved
            }
            return fetch(tweet)
        }

        private suspend fun fetch(tweet: Tweet): OGP = GlobalScope.async(Dispatchers.Default) {
            val url = tweet.ext.urls.first().expanded
            try {
                val document = Jsoup.connect(url).get()
                return@async OGP().apply {
                    this.tweetId = tweet.id
                    this.url = url
                    this.imageUrl = findContent(document, "property", "og:image")
                    this.title = findContent(document, "property", "og:title")
                            ?: document.title()
                    this.description = findContent(document, "property", "og:description")
                            ?: findContent(document, "name", "description")
                                    ?: document.location()
                }.save()
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (exception: Exception) {
                w(exception)
                return@async OGP().apply {
                    this.tweetId = tweet.id
                    this.url = url
                    this.hasError = true
                }.save()
            }
        }.await()

        private fun findContent(document: Document, attrKey: String, attrValue: String): String? {
            val elements = document.getElementsByAttributeValue(attrKey, attrValue)
            return if (elements.hasAttr("content")) {
                elements.attr("content")
            } else {
                null
            }
        }
    }

    fun save(): OGP {
        expiredAt = System.currentTimeMillis() + 86400L
        box.put(this)
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is OGP
                && this.tweetId == other.tweetId
    }

    override fun hashCode(): Int {
        return tweetId.hashCode()
    }
}
