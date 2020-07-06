package com.example.photorama.heplerObjects

/**
 * @author Sultan
 * a custom object that's used to store a comment's info that can be acquired from different
 * server requests.
 */

data class CommentType(
    var commentId: String,
    var comment: String,
    var postId: String,
    var userId: String,
    var username: String,
    var userScreenName: String,
    var userAvatar: String?,
    var datetime: String
)