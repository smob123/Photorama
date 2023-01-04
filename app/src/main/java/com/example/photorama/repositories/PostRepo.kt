package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.CommentType
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Mutations
import com.example.photorama.networking.Queries
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostRepo private constructor(private val context: Context) {
    companion object {

        private lateinit var instance: PostRepo

        fun getInstance(context: Context): PostRepo {
            if (!::instance.isInitialized) {
                instance = PostRepo(context)
            }

            return instance
        }
    }

    lateinit var errorMessage: ErrorMessage

    suspend fun fetchPostInfo(postId: String): PostType? =
        suspendCoroutine {
            Queries(context).getPostById(postId, onCompleted = { err, res ->
                if (err != null || res == null || res.postById == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getPostById
                }

                val post = res.postById!!

                val description = post.description() ?: ""

                val postInfo = PostType(
                    postId,
                    post.userId(),
                    post.username(),
                    post.userScreenName(),
                    post.userAvatar(),
                    post.image(),
                    post.likes()!!,
                    post.comments()!!,
                    post.datetime(),
                    description
                )

                it.resume(postInfo)
            })
        }

    suspend fun fetchPostComments(
        postId: String,
        startIndex: Int,
        endIndex: Int
    ): ArrayList<CommentType>? =
        suspendCoroutine {
            Queries(context).getPostComments(
                postId,
                startIndex,
                endIndex,
                onCompleted = { err, res ->
                    if (err != null || res == null || res.postComments == null) {
                        errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        it.resume(null)
                        return@getPostComments
                    }

                    val comments = ArrayList<CommentType>()
                    val list = res.postComments!!

                    for (comment in list) {
                        val c = CommentType(
                            comment.id()!!,
                            comment.comment()!!,
                            postId,
                            comment.userId()!!,
                            comment.username()!!,
                            comment.userScreenName()!!,
                            comment.userAvatar()!!,
                            comment.datetime()!!
                        )

                        comments.add(c)
                    }

                    it.resume(comments)
                })
        }

    suspend fun sendComment(postId: String, comment: String): CommentType? =
        suspendCoroutine {
            Mutations(context).addComment(postId, comment, onCompleted = { err, res ->
                if (err != null || res == null || res.addComment() == null) {
                    errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@addComment
                }

                val commentInfo = res.addComment()!!
                val commentType = CommentType(
                    commentInfo.id()!!,
                    commentInfo.comment()!!,
                    commentInfo.postId()!!,
                    commentInfo.userId()!!,
                    commentInfo.username()!!,
                    commentInfo.userScreenName()!!,
                    commentInfo.userAvatar()!!,
                    commentInfo.datetime()!!
                )

                it.resume(commentType)
            })
        }

    suspend fun removeComment(commentId: String): Boolean =
        suspendCoroutine {
            Mutations(context)
                .deleteComment(
                    commentId,
                    onCompleted = { err, res ->
                        if (err != null || res == null || res.deleteComment() == null) {
                            errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                            it.resume(false)
                            return@deleteComment
                        }

                        it.resume(res.deleteComment()!!)
                    })
        }

    suspend fun likePost(postId: String): Boolean =
        suspendCoroutine {
            Mutations(context)
                .likePost(postId,
                    onCompleted = { err, res ->
                        if (err != null || res == null || res.likePost() == null) {
                            errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                            it.resume(false)
                            return@likePost
                        }

                        it.resume(res.likePost()!!)
                    })
        }

    suspend fun unlikePost(postId: String): Boolean =
        suspendCoroutine {
            Mutations(context)
                .unlikePost(postId,
                    onCompleted = { err, res ->
                        if (err != null || res == null || res.unlikePost() == null) {
                            errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                            it.resume(false)
                            return@unlikePost
                        }

                        it.resume(res.unlikePost()!!)
                    })
        }

    suspend fun deletePost(postId: String): Boolean =
        suspendCoroutine {
            Mutations(context)
                .deletePost(postId, onCompleted = { err, res ->
                    if (err != null || res == null || res.deletePost() == null) {
                        errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        it.resume(false)
                        return@deletePost
                    }

                    it.resume(res.deletePost()!!)
                })
        }

    suspend fun uploadPost(base64Image: String, description: String): Boolean =
        suspendCoroutine {
            Mutations(context).uploadPost(base64Image, description,
                onCompleted = { err, res ->
                    if (err != null || res == null || res.uploadPost() == null) {
                        errorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        it.resume(false)
                        return@uploadPost
                    }

                    it.resume(true)
                }
            )
        }
}
