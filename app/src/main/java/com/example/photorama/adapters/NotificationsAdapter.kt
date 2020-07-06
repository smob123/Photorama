package com.example.photorama.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photorama.PostViewActivity
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.heplerObjects.NotificationType
import com.example.photorama.heplerObjects.TextUtils
import com.example.photorama.networking.ServerDomain
import java.util.*

/**
 * @author Sultan
 * handles storing notifications to be displayed on the notifications screen.
 */

class NotificationsAdapter(
    private val context: Context,
    private var notifications: ArrayList<NotificationType>
) :
    RecyclerView.Adapter<NotificationsAdapter.MyHolder>() {

    class MyHolder(val view: ViewGroup) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false)
        return MyHolder(view as ViewGroup)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val view = holder.view
        val notification = notifications[position]

        // set the notification's icon, as the avatar of the user whom the notification came from
        val iconImageView = view.findViewById<ImageView>(R.id.notification_icon)
        if (notification.userAvatar != null) {
            Glide.with(context)
                .load("${ServerDomain().baseUrlString()}${notification.userAvatar}")
                .into(iconImageView)
        } else {
            iconImageView.setImageResource(R.drawable.avatar)
        }

        // set the notification's message text view
        val messageTextView = view.findViewById<TextView>(R.id.notification_message)
        messageTextView.text = notification.message

        // check if the notification is about a post, and display that post's image too
        val postImageView = view.findViewById<ImageView>(R.id.post_image)
        if (notification.type == NotificationType.TYPE.POST) {
            Glide.with(context).load("${ServerDomain().baseUrlString()}${notification.postImage}")
                .into(postImageView)
        }

        // display when the notification was sent
        val dateTextView = view.findViewById<TextView>(R.id.notification_date)
        dateTextView.text = TextUtils(context).getTimeDiff(notification.datetime)

        // set the on click listener for the notification
        view.setOnClickListener {
            // if the notification is about a post
            if (notification.type == NotificationType.TYPE.POST) {
                // then go to it
                goToPost(notification.postId!!)
            }
            // otherwise if it's about a user
            if (notification.type == NotificationType.TYPE.NEW_FOLLOWER) {
                // then go to their profile
                goToProfile(notification.followerName!!)
            }
        }
    }

    /**
     * goes to a post view activity.
     * @param postId the id of the post to display on the activity
     */
    private fun goToPost(postId: String) {
        val intent = Intent(context, PostViewActivity::class.java)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

    /**
     * goes to to search activity, to display the user profile fragment
     * @param username the username of the user that we want to show their profile
     */
    private fun goToProfile(username: String) {
        val intent = Intent(context, SearchActivity::class.java)
        intent.putExtra("username", username)
        context.startActivity(intent)
    }

    fun setItems(newList: ArrayList<NotificationType>) {
        notifications = newList
    }

    /**
     * adds new notifications to the adapter.
     * @param newList the list of new items to add
     */
    fun addItems(newList: ArrayList<NotificationType>) {
        notifications.addAll(newList)
    }
}
