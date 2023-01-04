package com.example.photorama.heplerObjects


class NotificationType(
    var userAvatar: String?,
    var datetime: String,
    var message: String,
    var postId: String?,
    var postImage: String?,
    var followerName: String?,
    var type: TYPE
) {
    enum class TYPE {
        POST,
        NEW_FOLLOWER
    }
}
