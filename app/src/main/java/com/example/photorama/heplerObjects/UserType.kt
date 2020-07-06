package com.example.photorama.heplerObjects

/**
 * stores a user's data, where all values are final except for avatar, since it's the only value that
 * can actually change.
 */

data class UserType(
    val userId: String,
    val username: String,
    val screenName: String,
    var avatar: String,
    val posts: List<String>,
    val followers: ArrayList<String>,
    val following: ArrayList<String>
)
