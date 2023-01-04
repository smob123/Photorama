package com.example.photorama.custom_ui

import android.content.Context
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.heplerObjects.CommentType
import com.example.photorama.heplerObjects.TextUtils
import com.example.photorama.networking.ServerDomain

class CommentView(private val mContext: Context) :
    LinearLayout(mContext), View.OnLongClickListener {

    init {
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        this.layoutParams = layoutParams
    }

    /**
     * sets the UI based on the comment's info
     * @param comment the comment's info to display on the UI
     */
    fun setCommentInfo(comment: CommentType) {
        val commentView =
            LayoutInflater.from(mContext).inflate(R.layout.post_comment_layout, this, false)

        // check if the user has an avatar
        val imgView = commentView.findViewById<ImageView>(R.id.user_avatar)
        if (comment.userAvatar != null) {
            val activity = mContext as AppCompatActivity
            activity.runOnUiThread {
                Glide.with(mContext).load("${ServerDomain().baseUrlString()}${comment.userAvatar}")
                    .into(imgView)
            }
        } else {
            imgView.setImageResource(R.drawable.avatar)
        }

        // update the text view to display the username
        val usernameTxtView = commentView.findViewById<TextView>(R.id.username_txt_view)
        usernameTxtView.text = comment.userScreenName

        // display the user's comment
        val commentTxtView = commentView.findViewById<TextView>(R.id.comment)
        val spannableComment = TextUtils(mContext).getMentionsAndHashtags(comment.comment)
        commentTxtView.text = spannableComment
        commentTxtView.movementMethod = LinkMovementMethod.getInstance()

        // update the date and time of the comment
        val datetime = commentView.findViewById<TextView>(R.id.datetime)
        datetime.text = TextUtils(mContext).getTimeDiff(comment.datetime)

        // set click listeners
        val userInfo = commentView.findViewById<ViewGroup>(R.id.user_info_container)
        userInfo.setOnClickListener {
            goToUserProfile(comment.username)
        }

        addView(commentView)
    }

    /**
     * goes to the commenter's profile.
     * @param username the username of the commenter
     */
    private fun goToUserProfile(username: String) {
        val intent = Intent(mContext, SearchActivity::class.java)
        intent.putExtra("username", username)

        mContext.startActivity(intent)
    }

    override fun onLongClick(p0: View?): Boolean {
        return true
    }
}
