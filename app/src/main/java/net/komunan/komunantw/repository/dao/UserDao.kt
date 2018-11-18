package net.komunan.komunantw.repository.dao

import androidx.room.*
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.common.service.TwitterService
import twitter4j.TwitterException

@Suppress("FunctionName")
@Dao
abstract class UserDao {

    /* ==================== Functions. ==================== */

    fun find(id: Long, credentials: List<Credential>, forceFetch: Boolean = false): User? {
        return if (forceFetch) {
            fetchUser(id, credentials) ?: find(id)
        } else {
            find(id) ?: fetchUser(id, credentials)
        }
    }

    fun save(user: User): User {
        assert(!user.dummy)
        pSave(user)
        d { "save: $user" }
        return user
    }

    private fun fetchUser(id: Long, credentials: List<Credential>): User? {
        return try {
            User( TwitterService.twitter(credentials.random()).showUser(id)).save()
        } catch (e: TwitterException) {
            w(e); null
        }
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_FIND = "SELECT * FROM user WHERE id = :id"
        private const val QUERY_DELETE_UNNECESSARY = "DELETE FROM user WHERE NOT EXISTS (SELECT * FROM tweet WHERE tweet.user_id = user.id OR tweet.reply_user_id = user.id OR tweet.rt_user_id = user.id OR tweet.qt_user_id = user.id)"
    }

    @Query(QUERY_FIND)
    abstract fun find(id: Long): User?

    @Query(QUERY_DELETE_UNNECESSARY)
    abstract fun deleteUnnecessary(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(users: List<User>)

    /* ==================== SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun pSave(user: User)
}
