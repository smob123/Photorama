package com.example.photorama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.photorama.R
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.networking.ServerDomain

/**
 * @author Sultan
 * stores a list of users to display their info.
 */

class UserListAdapter(
    private val mContext: Context,
    private val users: ArrayList<UserListItemType>
) : ArrayAdapter<UserListItemType>(mContext, 0, users),
    AdapterView.OnItemClickListener {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if (view == null) {
            view =
                LayoutInflater.from(mContext).inflate(R.layout.user_info_list_item, parent, false)
        }

        // get the user at the selected position
        val user = users[position]

        // update the screen name text view
        val screenNameTxtView = view!!.findViewById<TextView>(R.id.screen_name_txt_view)
        screenNameTxtView.text = user.screenName

        // update the username text view
        val usernameTxtView = view.findViewById<TextView>(R.id.username_txt_view)
        usernameTxtView.text = "@${user.username}"

        val imgView = view.findViewById<ImageView>(R.id.avatar_img)
        // if the user has an avatar
        val avatar = user.avatar
        if (avatar != "null") {
            // update the image view to display it
            Glide.with(mContext).load("${ServerDomain().baseUrlString()}${avatar}").into(imgView)
        } else {
            imgView.setImageResource(R.drawable.avatar)
        }

        return view
    }

    fun setValues(newUsers: ArrayList<UserListItemType>) {
        users.clear()
        users.addAll(newUsers)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, p2: Int, id: Long) {}

    /* this filter is used by the multi auto complete text view in order to return
       the username of the users to the view */

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = users
                results.count = users.size

                return results
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {}

            override fun convertResultToString(resultValue: Any?): CharSequence {
                if (resultValue != null) {
                    val result = resultValue as UserListItemType
                    return "@${result.username}"
                }

                return super.convertResultToString(resultValue)
            }
        }
        return filter
    }
}
