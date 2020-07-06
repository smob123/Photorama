package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Mutations
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserInteractionsRepo(private val context: Context) {

    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: UserInteractionsRepo

        fun getInstance(context: Context): UserInteractionsRepo {
            if (!this::instance.isInitialized) {
                instance = UserInteractionsRepo(context)
            }

            return instance
        }
    }

    private val cacheHandler = CacheHandler(context)
    lateinit var errorMessage: ErrorMessage

    /**
     * sends a request to follow an account.
     * @return whether the request was successful or not
     */
    suspend fun followUser(userId: String): Boolean =
        suspendCoroutine {
            Mutations(context).follow(userId, onCompleted = { err, res ->
                if (err != null || res == null || res.follow() == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(false)
                    return@follow
                }

                it.resume(res.follow()!!)
            })
        }

    /**
     * sends a request to unfollow an account.
     * @return whether the request was successful or not
     */
    suspend fun unfollowUser(userId: String): Boolean =
        suspendCoroutine {
            Mutations(context).unfollow(userId, onCompleted = { err, res ->
                if (err != null || res == null || res.unfollow() == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(false)
                    return@unfollow
                }

                it.resume(res.unfollow()!!)
            })
        }

    /**
     * returns the cached user's info.
     */
    fun getCachedInfo(): UserType {
        val cache = cacheHandler.getCache()

        val posts = ArrayList<String>()
        val cachedPosts = cache.getJSONArray("posts")
        for (i in 0 until cachedPosts.length()) {
            posts.add(cachedPosts[i].toString())
        }

        val following = ArrayList<String>()
        val cachedFollowing = cache.getJSONArray("following")
        for (i in 0 until cachedFollowing.length()) {
            following.add(cachedFollowing[i].toString())
        }

        val followers = ArrayList<String>()
        val cachedFollowers = cache.getJSONArray("followers")
        for (i in 0 until cachedFollowers.length()) {
            followers.add(cachedFollowers[i].toString())
        }

        val info = UserType(
            cache.getString("id"),
            cache.getString("username"),
            cache.getString("screenName"),
            cache.getString("avatar"),
            posts,
            followers,
            following
        )

        return info
    }
}
