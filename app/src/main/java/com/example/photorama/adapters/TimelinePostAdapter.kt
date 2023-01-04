package com.example.photorama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.R
import com.example.photorama.custom_ui.PostLayout
import com.example.photorama.heplerObjects.PostType

/**
 * @author Sultan
 * stores the user's timeline posts.
 */

class TimelinePostAdapter(
    private val context: Context,
    private var postList: ArrayList<PostType>
) :
    RecyclerView.Adapter<TimelinePostAdapter.MyHolder>() {
    class MyHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.timeline_post_layout, parent, false)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        // get the time line post view
        val postView = holder.view
        // get the container in the post view
        val container = postView.findViewById<CardView>(R.id.timeline_post_container)

        // initialize a new post layout, and add it to the container
        val postLayout = PostLayout(
            context as AppCompatActivity,
            context.applicationContext,
            postList[position]
        )

        container.addView(postLayout)
    }

    override fun onViewRecycled(holder: MyHolder) {
        super.onViewRecycled(holder)
        // remove all views to prevent over drawing
        val view = holder.view as ViewGroup
        view.removeAllViews()
    }

    /**
     * adds new timeline items to the adapter.
     * @param newPosts the list of new posts to add
     */
    fun setItems(newPosts: ArrayList<PostType>) {
        postList = newPosts
    }

    /**
     * adds new timeline item to the adapter.
     * @param newPost the new post to add
     */
    fun addItem(newPost: PostType) {
        postList.add(newPost)
    }

    /**
     * this method is used by the child views, in order to notify the adapter that the user
     * has deleted the post.
     * @param postId the id of the post that the user has deleted
     */
    fun removeItem(index: Int) {
        postList.removeAt(index)
    }

    /**
     * returns the list of posts.
     */
    fun getItems() = postList
}
