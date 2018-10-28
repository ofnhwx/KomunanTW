package net.komunan.komunantw.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.common.draweeView
import net.komunan.komunantw.common.string
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import org.jetbrains.anko.*

internal class TweetUI: AnkoComponent<ViewGroup> {
    lateinit var userIcon: ImageView
    lateinit var userName: TextView
    lateinit var screenName: TextView
    lateinit var tweetDateTime: TextView
    lateinit var tweetText: TextView

    @SuppressLint("ResourceType")
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            userIcon = draweeView {
                id = R.id.tweet_user_icon
            }.lparams(dip(48), dip(48))
            verticalLayout {
                linearLayout {
                    userName = textView {
                        id = R.id.tweet_user_name
                    }
                    screenName = textView {
                        id = R.id.tweet_user_screen_name
                    }
                    tweetDateTime = textView {
                        id = R.id.tweet_date_time
                    }
                }
                tweetText = textView {
                    id = R.id.tweet_text
                }
            }
        }
    }

    fun bind(tweet: TweetDetail) {
        launch(UI) {
            val user = async(CommonPool) { User.find(tweet.userId) }
            userIcon.setImageURI(Uri.parse(user.await().imageUrl))
            userName.text = user.await().name
            screenName.text = R.string.format_screen_name.string(user.await().screenName)
        }
        tweetDateTime.text = "test"
        tweetText.text = tweet.text
    }
}
