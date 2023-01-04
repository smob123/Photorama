package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Queries
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TimelineRepo(private val context: Context) {

    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: TimelineRepo

        fun getInstance(context: Context): TimelineRepo {
            if (!this::instance.isInitialized) {
                instance = TimelineRepo(context)
            }

            return instance
        }
    }

    val cacheHandler = CacheHandler(context)
    lateinit var errorMessage: ErrorMessage

    /**
     * fetches the user's from the server.
     */
    suspend fun fetchPosts(startIndex: Int, endIndex: Int): ArrayList<PostType>? =
        // suspend coroutine, which only resumes when the server responds to the request successfully
        suspendCoroutine {
            Queries(context).getTimeLine(startIndex, endIndex, onCompleted = { err, res ->

                // throw an error if the request isn't successful
                if (err != null || res == null || res.userTimeline == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getTimeLine
                }

                val posts = ArrayList<PostType>()

                // otherwise parse the data, and add it into the list of posts
                for (post in res.userTimeline!!) {
                    val postType = PostType(
                        post.id().toString(),
                        post.userId(),
                        post.username(),
                        post.userScreenName(),
                        post.userAvatar(),
                        post.image(),
                        post.likes() as List<String>,
                        post.comments() as List<String>,
                        post.datetime(),
                        post.description().toString()
                    )

                    posts.add(postType)
                }

                if (startIndex == 0) {
                    storeCache(posts)
                }

                it.resume(posts)
            })
        }

    /**
     * deletes a post from the user's timeline, and updates the cache.
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

        storeCache(posts.take(5) as ArrayList<PostType>)
        return found
    }

    private fun storeCache(posts: ArrayList<PostType>) {
        val jsonArray = JSONArray()
        for (post in posts) {
            val jsonObj = JSONObject()
            jsonObj.put("id", post.id)
            jsonObj.put("userId", post.userId)
            jsonObj.put("username", post.username)
            jsonObj.put("userScreenName", post.userScreenName)
            jsonObj.put("userAvatarUrl", post.userAvatarUrl)
            jsonObj.put("image", post.image)

            val likes = JSONArray()
            for (like in post.likes) {
                likes.put(like)
            }
            jsonObj.put("likes", likes)

            val comments = JSONArray()
            for (comment in post.comments) {
                comments.put(comment)
            }
            jsonObj.put("comments", comments)

            jsonObj.put("datetime", post.datetime)
            jsonObj.put("description", post.description)

            jsonArray.put(jsonObj)
        }

        cacheHandler.storeTimelineCache(jsonArray)
    }

    fun getCache(): ArrayList<PostType> {
        val cache = cacheHandler.getTimelineCache()
        val posts = ArrayList<PostType>()

        for (i in 0 until cache.length()) {
            val p = cache.getJSONObject(i)
            val cachedLikes = p.getJSONArray("likes")
            val likes = ArrayList<String>()

            for (j in 0 until cachedLikes.length()) {
                likes.add(cachedLikes.getString(j))
            }

            val cachedComments = p.getJSONArray("likes")
            val comments = ArrayList<String>()

            for (j in 0 until cachedComments.length()) {
                comments.add(cachedComments.getString(j))
            }

            val post = PostType(
                p.getString("id"),
                p.getString("userId"),
                p.getString("username"),
                p.getString("userScreenName"),
                p.getString("userAvatarUrl"),
                p.getString("image"),
                likes,
                comments,
                p.getString("datetime"),
                p.getString("description")
            )

            posts.add(post)
        }

        return posts
    }
}
