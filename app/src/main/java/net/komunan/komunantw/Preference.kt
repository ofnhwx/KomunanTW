package net.komunan.komunantw

import com.marcinmoskala.kotlinpreferences.PreferenceHolder
import net.komunan.komunantw.repository.entity.ConsumerKeySecret
import twitter4j.auth.RequestToken

object Preference: PreferenceHolder() {
    var useInMemoryDatabase: Boolean by bindToPreferenceField(false)

    private var requestTokenKey: String? by bindToPreferenceFieldNullable()
    private var requestTokenSecret: String? by bindToPreferenceFieldNullable()
    var requestToken: RequestToken?
        get() {
            val token = this.requestTokenKey
            val tokenSecret = this.requestTokenSecret
            return if (token == null || tokenSecret == null) null else RequestToken(token, tokenSecret)
        }
        set(value) {
            requestTokenKey = value?.token
            requestTokenSecret = value?.tokenSecret
        }

    private var consumerKey: String? by bindToPreferenceFieldNullable()
    private var consumerSecret: String? by bindToPreferenceFieldNullable()
    var consumerKeySecret: ConsumerKeySecret?
        get() {
            val consumerKey = this.consumerKey
            val consumerSecret = this.consumerSecret
            return if (consumerKey == null || consumerSecret == null) null else ConsumerKeySecret().apply {
                this.consumerKey = consumerKey
                this.consumerSecret = consumerSecret
            }
        }
        set(value) {
            consumerKey = value?.consumerKey
            consumerSecret = value?.consumerSecret
        }
}
