package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.HashtagType
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Queries
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchRepo(private val context: Context) {
    companion object {
        private lateinit var instance: SearchRepo
        fun getInstance(context: Context): SearchRepo {
            if (!::instance.isInitialized) {
                instance = SearchRepo(context)
            }

            return instance
        }
    }

    lateinit var hashtagSearchErrorMessage: ErrorMessage
    lateinit var userSearchErrorMessage: ErrorMessage
    lateinit var postSearchErrorMessage: ErrorMessage
    lateinit var userByIdSearchErrorMessage: ErrorMessage

    suspend fun searchUserByName(username: String): ArrayList<UserType>? =
        suspendCoroutine {
            Queries(context).searchUserByName(username, onCompleted = { err, res ->
                if (err != null || res == null || res.searchUsersByName() == null) {
                    hashtagSearchErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@searchUserByName
                }

                val results = ArrayList<UserType>()

                for (result in res.searchUsersByName()!!) {
                    val user = UserType(
                        result.id()!!,
                        result.username()!!,
                        result.screenName()!!,
                        result.avatar()!!,
                        ArrayList(),
                        ArrayList(),
                        ArrayList()
                    )
                    results.add(user)
                }

                it.resume(results)
            })
        }

    suspend fun searchHashtag(hashtag: String): ArrayList<HashtagType>? =
        suspendCoroutine {
            Queries(context).searchHashtags(hashtag, onCompleted = { err, res ->
                if (err != null || res == null || res.searchHashtagsByName() == null) {
                    userSearchErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@searchHashtags
                }

                val hashtags = ArrayList<HashtagType>()

                for (result in res.searchHashtagsByName()!!) {
                    val hashtagType = HashtagType(
                        result.id()!!,
                        result.hashtag()!!,
                        result.numberOfPosts()!!
                    )
                    hashtags.add(hashtagType)
                }

                it.resume(hashtags)
            })
        }

    suspend fun searchPostsByHashtag(hashtag: String): ArrayList<PostType>? =
        suspendCoroutine {
            Queries(context).getPostsByHashtag(hashtag, onCompleted = { err, res ->
                if (err != null || res == null || res.postsByHashtag == null) {
                    postSearchErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getPostsByHashtag
                }

                val postsList = ArrayList<PostType>()

                for (post in res.postsByHashtag!!) {
                    val p = PostType(
                        post.id()!!,
                        post.userId(),
                        post.username(),
                        post.userScreenName(),
                        post.userAvatar(),
                        post.image(),
                        post.likes()!!,
                        post.comments()!!,
                        post.datetime(),
                        post.description() ?: ""
                    )
                    postsList.add(p)
                }

                it.resume(postsList)
            })
        }

    suspend fun searchUsersById(userIds: ArrayList<String>): ArrayList<UserListItemType>? =
        suspendCoroutine {
            Queries(context).getUsersByIds(userIds, onCompleted = { err, res ->
                if (err != null || res == null || res.usersByIds == null) {
                    userByIdSearchErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getUsersByIds
                }

                val usersList = ArrayList<UserListItemType>()

                for (item in res.usersByIds!!) {
                    val user = UserListItemType(
                        item.id()!!,
                        item.username()!!,
                        item.screenName()!!,
                        item.avatar()!!
                    )
                    usersList.add(user)
                }

                it.resume(usersList)
            })
        }
}
