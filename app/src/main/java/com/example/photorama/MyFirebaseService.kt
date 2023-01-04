package com.example.photorama

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.NotificationUtils
import com.example.photorama.viewModels.AuthViewModel
import com.example.photorama.viewModels.AuthViewModelFactory
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

/**
 * @author Sultan
 * handles displaying push notifications, and linking them to activities.
 */

class MyFirebaseService : FirebaseMessagingService() {
    // notification IDs
    private val DEFAULT_ID = 0
    private val LIKE_ID = 1
    private val COMMENT_ID = 2
    private val FOLLOW_ID = 3
    private val viewModel: AuthViewModel

    init {
        val factory = AuthViewModelFactory(this)
        viewModel = ViewModelProvider(
            applicationContext as ViewModelStoreOwner,
            factory
        ).get(AuthViewModel::class.java)

        initUploadTokenErrorObserver()
    }

    private fun initUploadTokenErrorObserver() {
        viewModel.getFirebaseTokenUploadError()
            .observe(applicationContext as LifecycleOwner, Observer { error ->
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
            })
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        // check if the user's credentials are stored in cache; ie, check if the user is logged in
        var userId: String? = null
        var jwt: String? = null

        val jsonObject = CacheHandler(this).getCache()
        if (jsonObject.has("id") && jsonObject.has("jwt")) {
            userId = jsonObject.get("id").toString()
            jwt = jsonObject.get("jwt").toString()
        }

        // send the new token to the server if the user is logged in, otherwise the token will be
        // sent later when the user submits a login form successfully
        if (userId != null && jwt != null) {
            viewModel.updateFirebaseToken(newToken)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            val data = remoteMessage.data

            // get the message's title, and body
            val title = data["title"]
            val body = data["body"]

            // verify that they are not null
            if (title != null && body != null) {
                val details = data["details"]
                val json = JSONObject(details)
                val pendingIntent: PendingIntent

                // check if the type of information that the message has, and initialize a pending
                // activity accordingly
                if (!json.isNull("postInfo")) {
                    val postInfo = json.get("postInfo") as JSONObject
                    pendingIntent = getPostViewIntent(postInfo)
                } else if (!json.isNull("userInfo")) {
                    val userInfo = json.get("userInfo") as JSONObject
                    pendingIntent = getUserProfileIntent(userInfo)
                } else {
                    val intent = Intent(this, MainAppActivity::class.java)
                    pendingIntent =
                        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                }

                // notification settings //

                // sound uri
                val soundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) as Uri

                // vibration frequency
                val vibrationFrequency = longArrayOf(1000, 1000, 1000, 1000, 1000)

                // display a notification
                val utils = NotificationUtils(this)
                val builder = utils.getChannelNotification(title, body)
                    .setSound(soundUri)
                    .setLights(ContextCompat.getColor(this, R.color.colorPrimary), 100, 100)
                    .setVibrate(vibrationFrequency)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                val notificationId: Int

                // decide the notification's id based on the message type
                if (body.contains("like")) {
                    notificationId = LIKE_ID
                } else if (body.contains("comment")) {
                    notificationId = COMMENT_ID
                } else if (body.contains("follow")) {
                    notificationId = FOLLOW_ID
                } else {
                    notificationId = DEFAULT_ID
                }

                utils.getManager().notify(notificationId, builder.build())
            }
        }
    }

    /**
     * initializes a pending post activity intent.
     * @param postInfo information about the post, that will be sent to the activity
     * @return a pending intent leading to the post that the notification links to
     */
    private fun getPostViewIntent(postInfo: JSONObject): PendingIntent {
        val pendingIntent: PendingIntent

        // check if the object contains a post id
        if (postInfo.isNull("postId")) {
            val intent = Intent(this, MainAppActivity::class.java)
            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        } else {
            val postId = postInfo.get("postId").toString()

            val intent = Intent(this, PostViewActivity::class.java)
            intent.putExtra("postId", postId)
            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        return pendingIntent
    }

    /**
     * initializes a pending search activity intent to display the user's info there.
     * @param userInfo information about the user, that will be sent to the activity
     * @return a pending intent leading to the user that the notification links to
     */
    private fun getUserProfileIntent(userInfo: JSONObject): PendingIntent {
        val pendingIntent: PendingIntent

        if (userInfo.isNull("username")) {
            val intent = Intent(this, MainAppActivity::class.java)
            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        } else {
            val username = userInfo.get("username").toString()

            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("username", username)

            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        return pendingIntent
    }
}
