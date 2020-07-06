package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Queries
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ExploreRepo(private val context: Context) {
    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: ExploreRepo

        fun getInstance(context: Context): ExploreRepo {
            if (!this::instance.isInitialized) {
                instance = ExploreRepo(context)
            }

            return instance
        }
    }

    lateinit var postSuggestionsErrorMessage: ErrorMessage

    /**
     * fetches the user's recommended posts from the server.
     */
    suspend fun fetchPosts(startIndex: Int, endIndex: Int): ArrayList<PostType>? =
        // suspend coroutine, which only resumes when the server responds to the request successfully
        suspendCoroutine<ArrayList<PostType>?> { it ->
            Queries(context).getPostRecommendations(
                startIndex,
                endIndex,
                onCompleted = { err, res ->

                    // throw an error if the request isn't successful
                    if (err != null || res == null || res.postRecommendations == null) {
                        postSuggestionsErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        it.resume(null)
                        return@getPostRecommendations
                    }

                    val posts = ArrayList<PostType>()
                    // otherwise parse the data, and add it into the list of posts
                    for (post in res.postRecommendations!!) {
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

                    it.resume(posts)
                })
        }
}
