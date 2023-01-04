package com.example.photorama.heplerObjects

/**
 * @author Sultan
 * a custom object that's used to store a post's info that can be acquired from different
 * server requests.
 */

data class PostType(
    var id: String,
    var userId: String,
    var username: String,
    var userScreenName: String,
    var userAvatarUrl: String,
    var image: String,
    var likes: List<String>,
    var comments: List<String>,
    var datetime: String,
    var description: String
)
