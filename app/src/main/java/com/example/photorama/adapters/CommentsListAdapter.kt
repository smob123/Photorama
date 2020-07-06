package com.example.photorama.adapters

import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.R
import com.example.photorama.custom_ui.CommentView
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.CommentType
import com.example.photorama.viewModels.PostViewModel
import kotlinx.android.synthetic.main.activity_post_view.view.*

/**
 * @author Sultan.
 * handles storing comment views in a list.
 */

class CommentsListAdapter(
    private val context: Context,
    private var comments: ArrayList<CommentType>,
    private val viewModel: PostViewModel
) :
    RecyclerView.Adapter<CommentsListAdapter.MyHolder>() {

    // the user's id
    private val userId: String

    init {
        // get the user's id from cache
        val json = CacheHandler(context).getCache()

        // initialize the user's id
        userId = json.get("id").toString()
    }

    class MyHolder(val view: CommentView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = CommentView(context)
        return MyHolder(view)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val commentView = holder.view
        // get the comment's data at the given position
        val comment = comments[position]
        // update the comment view
        commentView.setCommentInfo(comment)

        // set an on long click listener if the comment belongs to the user
        if (comments[position].userId == userId) {
            setCommentLongPressListener(commentView, position, comment.commentId)
        }
    }

    /**
     * displays a dialog box when long pressing on the comment.
     * @param view the comment view that was long pressed
     * @param viewIndex the index of the view in the adapter
     * @param commentId the id of the comment that's linked to the view
     */
    private fun setCommentLongPressListener(
        view: CommentView,
        viewIndex: Int,
        commentId: String
    ) {
        val activity = context as AppCompatActivity
        view.setOnLongClickListener { commentView ->
            // initialize the dialog box's options
            val options = arrayOf("Delete Comment?")
            val dialogBuilder = android.app.AlertDialog.Builder(context)
            dialogBuilder.setTitle("Choose an action")
            dialogBuilder.setItems(options, DialogInterface.OnClickListener { dialog, index ->
                when (index) {
                    0 -> {
                        // if the user clicks on yes
                        activity.runOnUiThread {
                            // remove the view from the adapter, and update the number of comments
                            // on the UI
                            removeItem(viewIndex)
                            val rootView = view.rootView as ViewGroup
                            val postLayout = rootView.post_content[0]
                            val numOfCommentsTxtView =
                                postLayout.findViewById<TextView>(R.id.num_of_comments)
                            val numOfComments = numOfCommentsTxtView.text.toString().toInt() - 1
                            numOfCommentsTxtView.text = numOfComments.toString()
                        }

                        // send a delete comment request to the server
                        viewModel.deleteComment(commentId)
                    }
                }
            }).show()

            return@setOnLongClickListener true
        }
    }

    override fun onViewRecycled(holder: MyHolder) {
        super.onViewRecycled(holder)
        // remove all views from the recycled view
        val view = holder.view
        view.removeAllViews()
    }

    /**
     * sets the list of items.
     */
    fun setItems(newItems: ArrayList<CommentType>) {
        comments = newItems
    }

    /**
     * adds new items to the adapter.
     * @param newItems list of new items to add
     */
    fun addItems(newItems: ArrayList<CommentType>) {
        comments.addAll(0, newItems)
        notifyDataSetChanged()
    }

    /**
     * removes an item at a given index from the adapter.
     * @param index the index of the item to remove
     */
    private fun removeItem(index: Int) {
        comments.removeAt(index)
        notifyDataSetChanged()
    }
}
