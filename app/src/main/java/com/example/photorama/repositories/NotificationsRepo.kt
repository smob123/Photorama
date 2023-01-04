package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.NotificationType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Queries
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationsRepo(private val context: Context) {

    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: NotificationsRepo

        fun getInstance(context: Context): NotificationsRepo {
            if (!this::instance.isInitialized) {
                instance = NotificationsRepo(context)
            }

            return instance
        }
    }

    private val cacheHandler = CacheHandler(context)
    lateinit var notificationsFetchErrorMessage: ErrorMessage

    /**
     * fetches the user's notifications from the server.
     */
    suspend fun fetchNotifications(
        startIndex: Int,
        endIndex: Int
    ): ArrayList<NotificationType>? =
        // suspend coroutine, which only resumes when the server responds to the request successfully
        suspendCoroutine { it ->
            Queries(context).getNotifications(startIndex, endIndex, onCompleted = { err, res ->

                // throw an error if the request isn't successful
                if (err != null || res == null || res.userNotifications == null) {
                    notificationsFetchErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@getNotifications
                }

                val notifications = ArrayList<NotificationType>()

                // otherwise parse the data, and add it into the list of posts
                for (notification in res.userNotifications!!) {

                    val notificationType: NotificationType.TYPE

                    if (notification.postId() != null) {
                        notificationType = NotificationType.TYPE.POST
                    } else {
                        notificationType = NotificationType.TYPE.NEW_FOLLOWER
                    }

                    val n = NotificationType(
                        notification.userAvatar(),
                        notification.datetime()!!,
                        notification.message()!!,
                        notification.postId(),
                        notification.postImage(),
                        notification.followerName(),
                        notificationType
                    )

                    notifications.add(n)
                }

                if (startIndex == 0) {
                    cacheNotifications(notifications)
                }

                it.resume(notifications)
            })
        }

    fun getCache(): ArrayList<NotificationType> {
        val notifications = ArrayList<NotificationType>()
        val jsonArray = cacheHandler.getNotificationsCache()

        for (i in 0 until jsonArray.length()) {
            val notification = jsonArray.getJSONObject(i)

            val datetime = notification.getString("datetime")
            val message = notification.getString("message")
            val postId: String?
            val postImage: String?
            val userAvatar: String?
            val followerName: String?
            var type = notification.get("type")

            if (type == NotificationType.TYPE.POST.toString()) {
                postId = notification.getString("postId")
                postImage = notification.getString("postImage")
                userAvatar = null
                followerName = null
                type = NotificationType.TYPE.POST
            } else {
                postId = null
                postImage = null
                followerName = notification.getString("followerName")
                type = NotificationType.TYPE.NEW_FOLLOWER

                if (notification.getString("userAvatar") != "null") {
                    userAvatar = notification.getString("userAvatar")
                } else {
                    userAvatar = null
                }
            }

            val n = NotificationType(
                userAvatar,
                datetime,
                message,
                postId,
                postImage,
                followerName,
                type
            )

            notifications.add(n)
        }

        return notifications
    }

    private fun cacheNotifications(notifications: ArrayList<NotificationType>) {
        val jsonArray = JSONArray()

        for (notification in notifications) {
            val jsonObj = JSONObject()
            jsonObj.put("postId", notification.postId)
            jsonObj.put("postImage", notification.postImage)
            jsonObj.put("userAvatar", notification.userAvatar)
            jsonObj.put("followerName", notification.followerName)
            jsonObj.put("datetime", notification.datetime)
            jsonObj.put("message", notification.message)
            jsonObj.put("type", notification.type)
            jsonArray.put(jsonObj)
        }

        cacheHandler.storeNotificationsCache(jsonArray)
    }
}