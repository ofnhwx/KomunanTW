package net.komunan.komunantw

import com.marcinmoskala.kotlinpreferences.PreferenceHolder
import net.komunan.komunantw.extension.gson
import net.komunan.komunantw.repository.entity.Consumer
import twitter4j.auth.RequestToken

@Suppress("ObjectPropertyName")
object Preference: PreferenceHolder() {
    var useInMemoryDatabase: Boolean by bindToPreferenceField(false)
    var currentPage: Int by bindToPreferenceField(0)
    var pageSize: Int by bindToPreferenceField(100)
    var fetchCount: Int by bindToPreferenceField(200)
    var fetchInterval: Long by bindToPreferenceField(180)
    var fetchIntervalThreshold: Float by bindToPreferenceField(0.8f)

    var fetchIntervalMillis: Long
        get() = fetchInterval * 1000
        set(value) {
            fetchInterval = value / 1000
        }

    private var _requestToken: String? by bindToPreferenceFieldNullable()
    private data class RequestTokenHolder(val token: String, val tokenSecret: String) {
        companion object {
            fun from(requestToken: RequestToken) = RequestTokenHolder(requestToken.token, requestToken.tokenSecret)
        }
        fun to() = RequestToken(token, tokenSecret)
    }
    var requestToken: RequestToken?
        get() = _requestToken?.let { gson.fromJson(it, RequestTokenHolder::class.java).to() }
        set(value) {
            _requestToken = value?.let { gson.toJson(RequestTokenHolder.from(it)).toString() }
        }

    private var _consumer: String? by bindToPreferenceFieldNullable()
    var consumer: Consumer?
        get() = _consumer?.let { gson.fromJson(it, Consumer::class.java) }
        set(value) {
            _consumer = value?.let { gson.toJson(value).toString() }
        }
}
