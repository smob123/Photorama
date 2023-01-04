package com.example.photorama.networking

import android.content.Context
import com.example.photorama.*
import com.example.photorama.heplerObjects.CacheHandler

/**
 * @author Sultan
 * sends mutation requests to the server.
 * @param context the app's context
 */

class Mutations(private val context: Context) {
    private lateinit var id: String
    private lateinit var jwt: String

    init {
        val jsonObject = CacheHandler(context).getCache()

        if (jsonObject.has("id") && jsonObject.has("jwt")) {
            id = jsonObject.get("id").toString()
            jwt = jsonObject.get("jwt").toString()
        }
    }

    fun signup(
        username: String,
        email: String,
        screenName: String,
        password: String,
        firebaseToken: String,
        onCompleted: (err: String?, data: SignupMutation.Data?) -> Unit
    ) {
        val mutation = SignupMutation.builder()
            .username(username)
            .screenName(screenName)
            .email(email)
            .password(password)
            .firebaseToken(firebaseToken)
            .build()

        QueryMutationRequests<SignupMutation.Data, SignupMutation.Data, SignupMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun login(
        username: String,
        password: String,
        firebaseToken: String,
        onCompleted: (err: String?, data: LoginMutation.Data?) -> Unit
    ) {
        val mutation = LoginMutation
            .builder()
            .username(username)
            .password(password)
            .firebaseToken(firebaseToken)
            .build()

        QueryMutationRequests<LoginMutation.Data, LoginMutation.Data, LoginMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }

    fun updateFirebaseToken(
        newToken: String,
        onCompleted: (err: String?, data: UpdateFirebaseTokenMutation.Data?) -> Unit
    ) {
        val mutation = UpdateFirebaseTokenMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .firebaseToken(newToken)
            .build()

        QueryMutationRequests<UpdateFirebaseTokenMutation.Data, UpdateFirebaseTokenMutation.Data,
                UpdateFirebaseTokenMutation.Variables>(context)
            .runMutation(mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }

    fun logout(onCompleted: (err: String?, data: LogoutMutation.Data?) -> Unit) {
        val mutation = LogoutMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .build()

        QueryMutationRequests<LogoutMutation.Data, LogoutMutation.Data, LogoutMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                })
    }

    fun uploadAvatar(
        base64ImageString: String?,
        onCompleted: (err: String?, data: UploadAvatarMutation.Data?) -> Unit
    ) {
        val mutation = UploadAvatarMutation.builder()
            .id(id)
            .jwt(jwt)
            .image(base64ImageString)
            .build()

        QueryMutationRequests<UploadAvatarMutation.Data, UploadAvatarMutation.Data, UploadAvatarMutation.Variables>(
            context
        )
            .runMutation(
                mutation, onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun deleteAvatar(onCompleted: (err: String?, data: DeleteAvatarMutation.Data?) -> Unit) {
        val mutation = DeleteAvatarMutation.builder()
            .userId(id)
            .jwt(jwt)
            .build()

        QueryMutationRequests<DeleteAvatarMutation.Data, DeleteAvatarMutation.Data, DeleteAvatarMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun uploadPost(
        image: String,
        description: String,
        onCompleted: (err: String?, data: UploadPostMutation.Data?) -> Unit
    ) {
        QueryMutationRequests<UploadPostMutation.Data, UploadPostMutation.Data, UploadPostMutation.Variables>(
            context
        )
            .runMutation(
                UploadPostMutation.builder()
                    .userId(id)
                    .jwt(jwt)
                    .image(image)
                    .description(description)
                    .build(),
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun deletePost(
        postId: String,
        onCompleted: (err: String?, data: DeletePostMutation.Data?) -> Unit
    ) {
        val mutation = DeletePostMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .postId(postId)
            .build()

        QueryMutationRequests<DeletePostMutation.Data, DeletePostMutation.Data, DeletePostMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun likePost(
        postId: String,
        onCompleted: (err: String?, data: LikePostMutation.Data?) -> Unit
    ) {
        val mutation = LikePostMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .postId(postId)
            .build()

        QueryMutationRequests<LikePostMutation.Data, LikePostMutation.Data, LikePostMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun unlikePost(
        postId: String,
        onCompleted: (err: String?, data: UnlikePostMutation.Data?) -> Unit
    ) {
        val mutation = UnlikePostMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .postId(postId)
            .build()

        QueryMutationRequests<UnlikePostMutation.Data, UnlikePostMutation.Data, UnlikePostMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun follow(
        userId: String,
        onCompleted: (err: String?, data: FollowUserMutation.Data?) -> Unit
    ) {
        val mutation = FollowUserMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .otherUserId(userId)
            .build()

        QueryMutationRequests<FollowUserMutation.Data, FollowUserMutation.Data, FollowUserMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun unfollow(
        userId: String,
        onCompleted: (err: String?, data: UnfollowUserMutation.Data?) -> Unit
    ) {
        val mutation = UnfollowUserMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .otherUserId(userId)
            .build()

        QueryMutationRequests<UnfollowUserMutation.Data, UnfollowUserMutation.Data, UnfollowUserMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun addComment(
        postId: String,
        comment: String,
        onCompleted: (err: String?, data: AddCommentMutation.Data?) -> Unit
    ) {
        val mutation = AddCommentMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .postId(postId)
            .comment(comment)
            .build()

        QueryMutationRequests<AddCommentMutation.Data, AddCommentMutation.Data, AddCommentMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }

    fun deleteComment(
        commentId: String,
        onCompleted: (err: String?, data: DeleteCommentMutation.Data?) -> Unit
    ) {
        val mutation = DeleteCommentMutation
            .builder()
            .userId(id)
            .jwt(jwt)
            .commentId(commentId)
            .build()

        QueryMutationRequests<DeleteCommentMutation.Data, DeleteCommentMutation.Data, DeleteCommentMutation.Variables>(
            context
        )
            .runMutation(
                mutation,
                onCompleted = { err, res ->
                    onCompleted(err, res)
                }
            )
    }
}
