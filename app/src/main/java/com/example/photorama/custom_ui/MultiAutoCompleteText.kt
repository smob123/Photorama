package com.example.photorama.custom_ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.MultiAutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.photorama.SearchHashtagsByNameQuery
import com.example.photorama.adapters.HashtagListAdapter
import com.example.photorama.adapters.UserListAdapter
import com.example.photorama.heplerObjects.AutoCompleteTokenizer
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.networking.Queries

/**
 * @author Sultan
 * Custom multiAutoCompleteTextView that uses a custom tokenizer, and makes query requests to the
 * server to ask for suggestions.
 *
 * @param context app's context
 * @param attributeSet view's attribute set
 */

class MultiAutoCompleteText(context: Context, attributeSet: AttributeSet) :
    MultiAutoCompleteTextView(context, attributeSet) {

    init {
        setTokenizer(AutoCompleteTokenizer())
        this.addTextChangedListener(checkForMentionsAndHashtags())
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
                if (newText != null && newText.trim() != "") {
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
                                getUserSuggestions(mention)
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
                                getHashtagSuggestions(hashtag)
                                return
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * gets user suggestions from the server.
     * @param mention the account that the user may want to mention
     */
    private fun getUserSuggestions(mention: String) {
        Queries(context)
            .searchUserByName(mention,
                onCompleted = { err, res ->
                    if (err != null) {
                        return@searchUserByName
                    }

                    if (res != null) {
                        // get the list of suggestions
                        val suggestions = res.searchUsersByName()
                        val filteredSuggestions =
                            ArrayList<UserListItemType>()
                        val activity = context as AppCompatActivity

                        for (item in suggestions!!) {
                            // only store users, who haven't been mentioned before
                            if (!text.contains(item.username()!!)) {
                                val user =
                                    UserListItemType(
                                        item.id()!!,
                                        item.username()!!,
                                        item.screenName()!!,
                                        item.avatar().toString()
                                    )
                                filteredSuggestions.add(user)
                            }
                        }

                        activity.runOnUiThread {
                            // set the adapter to display the suggestions,
                            // and show the dropdown menu
                            val adapter =
                                UserListAdapter(
                                    context,
                                    filteredSuggestions
                                )
                            setAdapter(adapter)
                            showDropDown()
                        }
                    }
                })
    }

    /**
     * gets hashtag suggestions from the server.
     * @param hashtag the hashtag that the user may want to mention
     */
    private fun getHashtagSuggestions(hashtag: String) {
        Queries(context)
            .searchHashtags(hashtag,
                onCompleted = { err, res ->
                    if (err != null) {
                        return@searchHashtags
                    }

                    if (res != null) {
                        // get the list of suggestions
                        val suggestions = res.searchHashtagsByName()
                        val filteredSuggestions =
                            ArrayList<SearchHashtagsByNameQuery.SearchHashtagsByName>()
                        val activity = context as AppCompatActivity

                        activity.runOnUiThread {
                            for (item in suggestions!!) {
                                // only store hashtags, who haven't been mentioned before
                                if (!text.contains(item.hashtag()!!)) {
                                    filteredSuggestions.add(item)
                                }
                            }

                            // set the adapter to display the suggestions,
                            // and show the dropdown menu
                            val adapter =
                                HashtagListAdapter(
                                    context,
                                    filteredSuggestions
                                )
                            setAdapter(adapter)
                            showDropDown()
                        }
                    }
                })
    }
}
