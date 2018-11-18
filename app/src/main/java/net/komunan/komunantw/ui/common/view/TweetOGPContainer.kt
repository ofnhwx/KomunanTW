package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.github.ajalt.timberkt.w
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.intentActionView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception

class TweetOGPContainer: LinearLayout {
    private val image: SimpleDraweeView
    private val title: TextView
    private val description: TextView

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_ogp, this, true)

        image = findViewById(R.id.image)
        title = findViewById(R.id.title)
        description = findViewById(R.id.description)

        titleColor = AppColor.WHITE
        descriptionColor = AppColor.GRAY
    }

    var titleColor: Int
        set(value) {
            title.setTextColor(value)
            field = value
        }

    var descriptionColor: Int
        set(value) {
            description.setTextColor(value)
            field = value
        }

    fun bind(url: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val document = withContext(Dispatchers.Default) { Jsoup.connect(url).get() }
                val imageUrl = getImageUrl(document)
                if (imageUrl == null) {
                    image.visibility = View.GONE
                } else {
                    image.visibility = View.VISIBLE
                    image.setImageURI(imageUrl)
                }
                title.text = getTitle(document)
                description.text = getDescription(document)
                setOnClickListener { url.intentActionView() }
            } catch (e: Exception) {
                w(e)
            }
            visibility = View.VISIBLE
        }
    }

    private fun getImageUrl(document: Document): String? {
        return findContent(document, "property", "og:image")
    }

    private fun getTitle(document: Document): String {
        return findContent(document, "property", "og:title") ?: document.title()
    }

    private fun getDescription(document: Document): String {
        return findContent(document, "property", "og:description")
                ?: findContent(document, "name", "description")
                ?: document.location()
    }

    private fun findContent(document: Document, attrKey: String, attrValue: String): String? {
        val elements = document.getElementsByAttributeValue(attrKey, attrValue)
        return if (elements.hasAttr("content")) {
            elements.attr("content")
        } else {
            null
        }
    }
}
