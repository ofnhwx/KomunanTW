package net.komunan.komunantw.repository.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.komunan.komunantw.repository.entity.TweetMedia

class TweetMediasConverter {
    private val gson = Gson()
    private val type by lazy { object: TypeToken<List<TweetMedia>>() {}.type }

    @TypeConverter
    fun fromList(value: List<TweetMedia>): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String?): List<TweetMedia> {
        return gson.fromJson(value, type)
    }
}
