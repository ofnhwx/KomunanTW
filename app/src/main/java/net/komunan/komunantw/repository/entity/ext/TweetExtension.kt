package net.komunan.komunantw.repository.entity.ext

import androidx.room.TypeConverter
import net.komunan.komunantw.common.extension.gson
import twitter4j.*

class TweetExtension() {
    var urls: List<Url> = emptyList()
    var medias: List<Media> = emptyList()
    var mentions: List<String> = emptyList()
    var hashtags: List<String> = emptyList()

    constructor(status: Status): this() {
        urls = Url.fromUrlEntities(status.urlEntities)
        medias = Media.fromMediaEntities(status.mediaEntities)
        mentions = status.userMentionEntities.map(UserMentionEntity::getScreenName)
        hashtags = status.hashtagEntities.map(HashtagEntity::getText)
    }

    class Converter {
        @TypeConverter
        fun fromJson(value: String): TweetExtension = gson.fromJson(value, TweetExtension::class.java)
        @TypeConverter
        fun toJson(value: TweetExtension): String = gson.toJson(value)
    }

    class Url() {
        var shorten : String = ""
        var display : String = ""
        var expanded: String = ""

        companion object {
            @JvmStatic
            fun fromUrlEntities(urlEntities: Array<URLEntity>): List<Url> {
                return urlEntities.map { Url(it) }
            }
        }

        constructor(urlEntity: URLEntity): this() {
            this.shorten = urlEntity.url
            this.display = urlEntity.displayURL
            this.expanded = urlEntity.expandedURL
        }
    }

    class Media() {
        var shorten : String = ""
        var display : String = ""
        var expanded: String = ""
        var url: String = ""
        var type: String = ""
        var videoAspectRatioHeight: Int = 0
        var videoAspectRatioWidth: Int = 0
        var videoDurationMillis: Long = 0
        var videoVariants: List<Video> = emptyList()

        companion object {
            @JvmStatic
            fun fromMediaEntities(mediaEntities: Array<MediaEntity>): List<Media> {
                return mediaEntities.map { Media(it) }
            }
        }

        constructor(mediaEntity: MediaEntity): this() {
            this.shorten = mediaEntity.url
            this.display = mediaEntity.displayURL
            this.expanded = mediaEntity.expandedURL
            this.url = mediaEntity.mediaURLHttps ?: mediaEntity.mediaURL
            this.type = mediaEntity.type
            if (this.isVideo) {
                this.videoAspectRatioHeight = mediaEntity.videoAspectRatioHeight
                this.videoAspectRatioWidth = mediaEntity.videoAspectRatioWidth
                this.videoDurationMillis = mediaEntity.videoDurationMillis
                this.videoVariants = mediaEntity.videoVariants.map { Video(it) }
            }
        }

        val isPhoto: Boolean
            get() = type == "photo"

        val isVideo: Boolean
            get() = type == "video"

        val isAnimatedGif: Boolean
            get() = type == "animated_gif"

        class Video() {
            var bitrate: Int = 0
            var contentType: String = ""
            var url: String = ""

            constructor(videoVariant: MediaEntity.Variant): this() {
                this.bitrate = videoVariant.bitrate
                this.contentType = videoVariant.contentType
                this.url = videoVariant.url
            }
        }
    }
}
