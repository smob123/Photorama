package com.example.photorama.custom_ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.photorama.adapters.HashtagListAdapter
import com.example.photorama.adapters.UserListAdapter
import com.example.photorama.heplerObjects.AutoCompleteTokenizer
import com.example.photorama.heplerObjects.HashtagType
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.viewModels.SearchViewModel
import com.example.photorama.viewModels.SearchViewModelFactory

/**
 * @author Sultan
 * Custom multiAutoCompleteTextView that uses a custom tokenizer, and makes query requests to the
 * server to ask for suggestions.
 *
 * @param context app's context
 * @param attributeSet view's attribute set
 */

class MultiAutoCompleteText(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView(context, attributeSet) {

    private val viewModel: SearchViewModel
    private lateinit var hashtagArrayAdapter: HashtagListAdapter
    private lateinit var userArrayAdapter: UserListAdapter

    init {
        val factory = SearchViewModelFactory(context)
        viewModel = ViewModelProvider(
            context as ViewModelStoreOwner,
            factory
        ).get(SearchViewModel::class.java)

        initAdapters()

        initHashtagSearchObserver()
        initUserSearchObserver()

        setTokenizer(AutoCompleteTokenizer())
        this.addTextChangedListener(checkForMentionsAndHashtags())
    }

    private fun initAdapters() {
        hashtagArrayAdapter = HashtagListAdapter(
            context,
            ArrayList()
        )

        userArrayAdapter = UserListAdapter(context, ArrayList())
    }

    private fun initHashtagSearchObserver() {
        viewModel.getHashtags().observe(context as LifecycleOwner, Observer { hashtags ->
            val hashtagList = ArrayList<HashtagType>()

            for (hashtag in hashtags) {
                // only suggest hashtags that haven't been mentioned before
                if (!this.text.contains(hashtag.hashtag)) {
                    hashtagList.add(hashtag)
                }
            }

            hashtagArrayAdapter.setValues(hashtagList)
            hashtagArrayAdapter.notifyDataSetChanged()
            this.setAdapter(hashtagArrayAdapter)
            this.showDropDown()
        })
    }

    private fun initUserSearchObserver() {
        viewModel.getUsers().observe(context as LifecycleOwner, Observer { users ->
            val usersList = ArrayList<UserListItemType>()

            for (user in users) {
                // only suggest users that haven't been mentioned before
                if (!this.text.contains(user.username)) {
                    val u =
                        UserListItemType(user.userId, user.username, user.screenName, user.avatar)
                    usersList.add(u)
                }
            }

            userArrayAdapter.setValues(usersList)
            userArrayAdapter.notifyDataSetChanged()
            this.setAdapter(userArrayAdapter)
            this.showDropDown()
        })
    }

    /**
     * check if the user is trying to mention a user, or a hashtag.
     */
    private fun checkForMentionsAndHashtags(): TextWatcher {
        return (object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(
                newText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // check that the text is not null, and is not empty
                if (newText == null || newText.trim() == "") {
                    return
                }
                // get the cursor's position
                val cursorPosition = selectionEnd

                // check if it's larger than 1
                if (cursorPosition > 1) {
                    // go from the cursor's position backwards, and look for
                    // mention, or hashtag symbols
                    for (i in (cursorPosition - 1) downTo 0) {
                        // dismiss the dropdown if nothing was found, or if a space character
                        // was encountered
                        if (i == 0 && newText[i] != '@' && newText[i] != '#') {
                            dismissDropDown()
                            return
                        }

                        if (Character.isSpaceChar(newText[i])) {
                            dismissDropDown()
                            return
                        }

                        // otherwise if a mention symbol was found
                        if (newText[i] == '@') {
                            // get the username, and get suggestions from the server
                            val mention =
                                newText.substring(
                                    i + 1,
                                    cursorPosition
                                )
                            viewModel.searchUsers(mention)
                            return
                        }

                        // or if a mention symbol was found
                        if (newText[i] == '#') {
                            // get the hashtag, and get suggestions from the server
                            val hashtag =
                                newText.substring(
                                    i + 1,
                                    cursorPosition
                                )
                            viewModel.searchHashtags(hashtag)
                            return
                        }
                    }
                }
            }
        })
    }
}
