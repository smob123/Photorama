package com.example.photorama.networking

import android.content.Context
import com.example.photorama.*
import com.example.photorama.heplerObjects.CacheHandler

/**
 * @author Sultan
 * sends query requests to the server.
 * @param context the app's context
 */

class Queries(private val context: Context) {
    private var id: String = ""
    private var jwt: String = ""

    init {
        val jsonObject = CacheHandler(context).getCache()
        if (jsonObject.has("id") && jsonObject.has("jwt")) {
            id = jsonObject.get("id").toString()
            jwt = jsonObject.get("jwt").toString()
        }
    }

    fun getUserByName(
        username: String,
        onCompleted: (err: String?, data: GetUserByNameQuery.Data?) -> Unit
    ) {
        val query = GetUserByNameQuery
            .builder()
            .username(username)
            .build()

        QueryMutationRequests<GetUserByNameQuery.Data, GetUserByNameQuery.Data, GetUserByNameQuery.Variables>(
            context
        )
            .runQuery(
                query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun getUsersByIds(
        userIds: ArrayList<String>,
        onCompleted: (err: String?, data: GetUsersByIdsQuery.Data?) -> Unit
    ) {
        val query = GetUsersByIdsQuery
            .builder()
            .userIds(userIds)
            .build()

        QueryMutationRequests<GetUsersByIdsQuery.Data, GetUsersByIdsQuery.Data, GetUsersByIdsQuery.Variables>(
            context
        )
            .runQuery(query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }

    fun getUserPosts(
        username: String,
        startIndex: Int,
        endIndex: Int,
        onCompleted: (err: String?, data: GetUserPostsQuery.Data?) -> Unit
    ) {
        val query = GetUserPostsQuery
            .builder()
            .username(username)
            .startIndex(startIndex)
            .endIndex(endIndex)
            .build()

        QueryMutationRequests<GetUserPostsQuery.Data, GetUserPostsQuery.Data, GetUserPostsQuery.Variables>(
            context
        )
            .runQuery(query, onCompleted = { err, res ->
                onCompleted(err, res)
            })
    }

    fun getPostById(id: String, onCompleted: (err: String?, data: GetPostByIdQuery.Data?) -> Unit) {
        val query = GetPostByIdQuery
            .builder()
            .postId(id)
            .build()

        QueryMutationRequests<GetPostByIdQuery.Data, GetPostByIdQuery.Data, GetPostByIdQuery.Variables>(
            context
        )
            .runQuery(query, onCompleted = { err, res ->
                onCompleted(err, res)
            })
    }

    fun getTimeLine(
        startIndex: Int, endIndex: Int,
        onCompleted: (err: String?, data: GetUserTimelineQuery.Data?) -> Unit
    ) {
        val query = GetUserTimelineQuery
            .builder()
            .userId(id)
            .jwt(jwt)
            .startIndex(startIndex)
            .endIndex(endIndex)
            .build()

        QueryMutationRequests<GetUserTimelineQuery.Data, GetUserTimelineQuery.Data, GetUserTimelineQuery.Variables>(
            context
        )
            .runQuery(query, onCompleted = { err, res ->
                onCompleted(err, res)
            })
    }

    fun searchUserByName(
        username: String,
        onCompleted: (err: String?, data: SearchUsersByNameQuery.Data?) -> Unit
    ) {
        val query = SearchUsersByNameQuery.builder()
            .username(username)
            .build()

        QueryMutationRequests<SearchUsersByNameQuery.Data, SearchUsersByNameQuery.Data, SearchUsersByNameQuery.Variables>(
            context
        )
            .runQuery(
                query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun getPostComments(
        postId: String,
        startIndex: Int,
        endIndex: Int,
        onCompleted: (err: String?, data: GetPostCommentsQuery.Data?) -> Unit
    ) {
        val query = GetPostCommentsQuery
            .builder()
            .postId(postId)
            .startIndex(startIndex)
            .endIndex(endIndex)
            .build()

        QueryMutationRequests<GetPostCommentsQuery.Data, GetPostCommentsQuery.Data, GetPostCommentsQuery.Variables>(
            context
        )
            .runQuery(query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }

    fun getPostRecommendations(
        startIndex: Int,
        endIndex: Int,
        onCompleted: (err: String?, data: GetPostRecommendationsQuery.Data?) -> Unit
    ) {
        val query = GetPostRecommendationsQuery
            .builder()
            .userId(id)
            .jwt(jwt)
            .startIndex(startIndex)
            .endIndex(endIndex)
            .build()

        QueryMutationRequests<GetPostRecommendationsQuery.Data, GetPostRecommendationsQuery.Data,
                GetPostRecommendationsQuery.Variables>(context)
            .runQuery(
                query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun searchHashtags(
        hashtag: String,
        onCompleted: (err: String?, data: SearchHashtagsByNameQuery.Data?) -> Unit
    ) {
        val query = SearchHashtagsByNameQuery
            .builder()
            .hashtag(hashtag)
            .build()

        QueryMutationRequests<SearchHashtagsByNameQuery.Data, SearchHashtagsByNameQuery.Data, SearchHashtagsByNameQuery.Variables>(
            context
        )
            .runQuery(
                query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun getPostsByHashtag(
        hashtag: String,
        onCompleted: (err: String?, data: GetPostsByHashtagQuery.Data?) -> Unit
    ) {
        val query = GetPostsByHashtagQuery
            .builder()
            .hashtag(hashtag)
            .build()

        QueryMutationRequests<GetPostsByHashtagQuery.Data, GetPostsByHashtagQuery.Data, GetPostsByHashtagQuery.Variables>(
            context
        )
            .runQuery(
                query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun getNotifications(
        startIndex: Int,
        endIndex: Int,
        onCompleted: (err: String?, data: GetUserNotificationsQuery.Data?) -> Unit
    ) {
        val query = GetUserNotificationsQuery
            .builder()
            .userId(id)
            .jwt(jwt)
            .startIndex(startIndex)
            .endIndex(endIndex)
            .build()

        QueryMutationRequests<GetUserNotificationsQuery.Data, GetUserNotificationsQuery.Data,
                GetUserNotificationsQuery.Variables>(context)
            .runQuery(query,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }
}
