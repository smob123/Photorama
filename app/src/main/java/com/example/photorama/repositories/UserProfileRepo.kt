package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Mutations
import com.example.photorama.networking.Queries
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserProfileRepo private constructor(private val context: Context) {

    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: UserProfileRepo

        fun getInstance(context: Context): UserProfileRepo {
            if (!this::instance.isInitialized) {
                instance = UserProfileRepo(context)
            }

            return instance
        }
    }

    val cacheHandler = CacheHandler(context)
    val mId = cacheHandler.getCache().getString("id")
    lateinit var errorMessage: ErrorMessage

    /**
     * fetches the user's profile info from the server.
     */
    suspend fun fetchUserInfo(username: String): UserType? =
        // suspend coroutine, which only resumes when the server responds to the request successfully
        suspendCoroutine {
            Queries(context).getUserByName(username, onCompleted = { err, res ->

                // throw an error if the request isn't successful
                if (err != null || res == null || res.userByName == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getUserByName
                }

                // otherwise parse the data, and add it into the list of posts
                val info = res.userByName!!
                var avatar = info.avatar()
                if (avatar == null) {
                    avatar = "null"
                }

                val following = ArrayList(info.following()!!)
                val followers = ArrayList(info.followers()!!)
                val userType = UserType(
                    info.id()!!,
                    info.username()!!,
                    info.screenName()!!,
                    avatar,
                    info.posts()!!,
                    followers,
                    following
                )

                if (userType.userId == mId) {
                    updateUserCache(userType)
                }

                it.resume(userType)
            })
        }

    /**
     * fetches the user's posts from the server.
     */
    suspend fun fetchPosts(
        userInfo: UserType,
        startIndex: Int,
        endIndex: Int
    ): ArrayList<PostType>? =
        // suspend coroutine, which only resumes when the server responds to the request successfully
        suspendCoroutine {
            Queries(context).getUserPosts(
                userInfo.username,
                startIndex,
                endIndex,
                onCompleted = { err, res ->
                    val posts = ArrayList<PostType>()

                    // throw an error if the request isn't successful
                    if (err != null || res == null || res.userPosts == null) {
                        errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        it.resume(null)
                        return@getUserPosts
                    }

                    // otherwise parse the data, and add it into the list of posts
                    for (post in res.userPosts!!) {
                        val postType = PostType(
                            post.id().toString(),
                            post.userId(),
                            post.username(),
                            userInfo.screenName,
                            userInfo.avatar,
                            post.image(),
                            post.likes() as List<String>,
                            post.comments() as List<String>,
                            post.datetime(),
                            post.description().toString()
                        )

                        posts.add(postType)
                    }

                    if (userInfo.userId == mId && startIndex == 0) {
                        storePostsCache(posts)
                    }

                    it.resume(posts)
                })
        }

    suspend fun uploadAvatar(base64Image: String?): Boolean =
        suspendCoroutine {
            Mutations(context).uploadAvatar(base64Image, onCompleted = { err, res ->
                if (err != null || res == null || res.uploadAvatar() == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(false)
                    return@uploadAvatar
                }

                it.resume(true)
            })
        }

    /**
     * sends a request to remove the user's avatar.
     * @return whether the request was successful or not
     */
    suspend fun removeAvatar(): Boolean =
        suspendCoroutine {
            Mutations(context).deleteAvatar(onCompleted = { err, res ->
                if (err != null || res == null || res.deleteAvatar() == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(false)
                    return@deleteAvatar
                }

                val removed = res.deleteAvatar()!!

                if (!removed) {
                    errorMessage = ErrorMessage(MESSAGE.REMOVE_AVATAR_ERROR)
                    return@deleteAvatar
                }

                it.resume(removed)
            })
        }

    /**
     * deletes a post from the user's profile, and updates the cache.
     */
    fun deletePost(postId: String, posts: ArrayList<PostType>): Boolean {
        var found = false
        for (post in posts) {
            if (post.id == postId) {
                found = true
                posts.remove(post)
                break
            }
        }

        storePostsCache(posts.take(5) as ArrayList<PostType>)
        return found
    }

    /**
     * updates the cached user's info.
     */
    private fun updateUserCache(userInfo: UserType) {
        val cache = cacheHandler.getCache()
        cache.put("avatar", userInfo.avatar)
        cache.put("following", JSONArray(userInfo.following))
        cache.put("followers", JSONArray(userInfo.followers))
        cache.put("posts", JSONArray(userInfo.posts))
        cacheHandler.storeLoginCache(cache)
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

    /**
     * stores the user's posts in cache.
     */
    private fun storePostsCache(posts: ArrayList<PostType>) {
        val jsonArray = JSONArray()

        for (post in posts) {
            val obj = JSONObject()
            obj.put("id", post.id)
            obj.put("userId", post.userId)
            obj.put("username", post.username)
            obj.put("userScreenName", post.userScreenName)
            obj.put("description", post.description)
            obj.put("image", post.image)
            obj.put("userAvatar", post.userAvatarUrl)
            obj.put("datetime", post.datetime)
            obj.put("likes", JSONArray(post.likes))
            obj.put("comments", JSONArray(post.comments))
            jsonArray.put(obj)
        }

        cacheHandler.storeProfilePostsCache(jsonArray)
    }

    /**
     * returns the user's posts from the cache.
     */
    fun getPostsCache(): ArrayList<PostType> {
        val cachedPosts = cacheHandler.getProfilePostsCache()
        val posts = ArrayList<PostType>()
        for (i in 0 until cachedPosts.length()) {
            val post = cachedPosts.getJSONObject(i)

            val cachedLikes = post.getJSONArray("likes")
            val likes = ArrayList<String>()
            for (j in 0 until cachedLikes.length()) {
                likes.add(cachedLikes[j].toString())
            }

            val cachedComments = post.getJSONArray("comments")
            val comments = ArrayList<String>()
            for (j in 0 until cachedComments.length()) {
                comments.add(cachedComments[j].toString())
            }

            val p = PostType(
                post.getString("id"),
                post.getString("userId"),
                post.getString("username"),
                post.getString("userScreenName"),
                post.getString("userAvatar"),
                post.getString("image"),
                likes,
                comments,
                post.getString("datetime"),
                post.getString("description")
            )

            posts.add(p)
        }

        return posts
    }
}
