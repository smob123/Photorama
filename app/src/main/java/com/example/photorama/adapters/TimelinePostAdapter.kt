package com.example.photorama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    context: Context,
    private var postList: ArrayList<PostType>
) :
    RecyclerView.Adapter<TimelinePostAdapter.MyHolder>() {
    private val mContext = context

    class MyHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.timeline_post_layout, parent, false)
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
            mContext,
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
    fun addItems(newPosts: ArrayList<PostType>) {
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }

    /**
     * this method is used by the child views, in order to notify the adapter that the user
     * has deleted the post.
     * @param postId the id of the post that the user has deleted
     */
    fun removeItem(postId: String) {
        for (i in 0 until postList.size) {
            val item = postList[i]
            if (item.id == postId) {
                postList.removeAt(i)
                notifyDataSetChanged()
                break
            }
        }
    }
}
