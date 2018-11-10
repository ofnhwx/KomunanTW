package net.komunan.komunantw.repository.dao

import androidx.room.*
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.w
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import twitter4j.TwitterException

@Suppress("FunctionName")
@Dao
abstract class UserDao {

    /* ==================== Functions. ==================== */

    fun find(id: Long, credential: Credential, forceFetch: Boolean = false): User? {
        return if (forceFetch) {
            fetchUser(id, credential) ?: find(id)
        } else {
            find(id) ?: fetchUser(id, credential)
        }
    }

    fun save(user: User): User {
        assert(!user.dummy)
        pSave(user)
        d { "save: $user" }
        return user
    }

    private fun fetchUser(id: Long, credential: Credential): User? {
        return try {
            User( TwitterService.twitter(credential).showUser(id)).save()
        } catch (e: TwitterException) {
            w(e); null
        }
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_FIND = "SELECT * FROM user WHERE id = :id"
    }

    @Query(QUERY_FIND)
    abstract fun find(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(users: List<User>)

    /* ==================== SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun pSave(user: User)
}
