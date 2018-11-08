package net.komunan.komunantw.repository.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.komunan.komunantw.repository.entity.TweetUrl

class TweetUrlsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<TweetUrl>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String): List<TweetUrl> {
        return gson.fromJson(value, object: TypeToken<List<TweetUrl>>() {}.type)
    }
}
