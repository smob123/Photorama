package com.example.photorama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.photorama.R
import com.example.photorama.SearchHashtagsByNameQuery
import com.example.photorama.heplerObjects.HashtagType

/**
 * @author Sultan
 * handles storing a list of hashtags.
 */

class HashtagListAdapter(
    private val mContext: Context,
    private val hashtags: ArrayList<HashtagType>
) : ArrayAdapter<HashtagType>(mContext, 0, hashtags),
    AdapterView.OnItemClickListener {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if (view == null) {
            view =
                LayoutInflater.from(mContext).inflate(R.layout.hashtag_item_layout, parent, false)
        }
        // get the hashtag at the current position
        val hashtagItem = hashtags[position]

        // update the text view that displayes the hashtag's name
        val hashtagTextView = view!!.findViewById<TextView>(R.id.hashtag_text_view)
        val hashtag = hashtagItem.hashtag
        hashtagTextView.text = hashtag

        // update the text view that displays the number of posts in the hashtag
        val numOfPostsTxtView = view.findViewById<TextView>(R.id.num_of_posts)
        val numOfPosts = hashtagItem.numOfPosts
        numOfPostsTxtView.text = numOfPosts.toString()

        return view
    }

    fun setValues(newHashtags: ArrayList<HashtagType>) {
        hashtags.clear()
        hashtags.addAll(newHashtags)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {}


    /* this filter is used by the multi auto complete text view in order to return
       the names of the hashtags to the view */

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = hashtags
                results.count = hashtags.size

                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {}

            override fun convertResultToString(resultValue: Any?): CharSequence {
                if (resultValue != null) {
                    // return the name of the hashtag
                    val result = resultValue as HashtagType
                    return result.hashtag
                }

                return super.convertResultToString(resultValue)
            }
        }
        return filter
    }
}
