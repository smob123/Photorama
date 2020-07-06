package com.example.photorama.adapters

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photorama.PostViewActivity
import com.example.photorama.custom_ui.SquareImage
import com.example.photorama.heplerObjects.PostParcelable
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ServerDomain

/**
 * @author Sultan
 * stores images to be displayed in a grid by a recycler view
 */

class GridViewAdapter(private val mContext: Context, private val posts: ArrayList<PostType>) :
    RecyclerView.Adapter<GridViewAdapter.MyHolder>() {

    class MyHolder(val view: CardView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = CardView(mContext)

        // set the layout's params
        val params = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(5)

        view.layoutParams = params

        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        // initialize the image view's container
        val container = holder.view
        container.radius = 15f

        // create a new image view
        val imageView = SquareImage(mContext)

        // resize the image to be a square
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        // get the image from the network in the background, and set it to the current image view
        val imageBitmap = posts[position].image
        Glide.with(mContext).load("${ServerDomain().baseUrlString()}$imageBitmap")
            .into(imageView)

        container.addView(imageView)

        container.setOnClickListener {
            viewPostActivity(posts[position], imageView)
        }
    }

    /**
     * goes to the post view activity to display the image there.
     * @param post the post's info
     * @param imageView the image view that's selected
     */
    private fun viewPostActivity(
        post: PostType,
        imageView: ImageView
    ) {
        // attach post's info to a parcelable object
        val postParcelable =
            PostParcelable(
                post.userId,
                post.username,
                post.userScreenName,
                post.id,
                post.userAvatarUrl,
                post.likes,
                post.comments,
                post.datetime,
                post.description
            )

        // create an intent for the next activity
        val intent = Intent(
            mContext,
            PostViewActivity::class.java
        )

        // add read uri permission, and pass the image's uri
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra("image", post.image)
        // pass the parcelable as an extra and go to the next activity
        intent.putExtra("postInfo", postParcelable)

        // animation options
        val options = ActivityOptions.makeSceneTransitionAnimation(
            mContext as AppCompatActivity,
            imageView,
            "hero_transition"
        )
        mContext.startActivity(intent, options.toBundle())
    }

    /**
     * sets the list of posts.
     */
    fun setItems(newPosts: ArrayList<PostType>) {
        posts.clear()
        posts.addAll(newPosts)
    }

    /**
     * adds new items to the adapter
     * @param newPosts the list of new posts to add
     */
    fun addItems(newPosts: List<PostType>) {
        posts.addAll(newPosts)
    }

    /**
     * this method is used by the child views, in order to notify the adapter that the user
     * has deleted the post.
     * @param postId the id of the post that the user has deleted
     */
    fun removeItem(index: Int) {
        posts.removeAt(index)
    }

    /**
     * returns the list of posts.
     */
    fun getItems() = posts

    override fun onViewRecycled(holder: MyHolder) {
        super.onViewRecycled(holder)
        val view = holder.view
        // remove all views fro the recycled view
        view.removeAllViews()
    }
}
