package com.example.photorama.heplerObjects

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.example.photorama.SearchActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Sultan
 * handles text manipulations.
 */

class TextUtils(private val mContext: Context) {

    /**
     * checks the passed text view for user, and hashtag mentions, highlights those mentions,
     * makes them clickable, and returns a the text as a spannable string
     *
     * @param text the text to check for user, and hashtag mentions
     *
     * @return the same text that's passed, but with the user, and hashtag mentions being
     * highlighted and clickable
     */
    fun getMentionsAndHashtags(text: String): SpannableString {
        // find all the mentions
        val regex = Regex("[#@]\\w+")
        val x = regex.findAll(text)
        val iterator = x.iterator()
        // create a spannable string of the text
        val ss = SpannableString(text)

        // go through the mentions
        while (iterator.hasNext()) {
            val res = iterator.next()
            val value = res.value
            val range = res.range

            // set the on click listener for the text
            if (value.startsWith("#")) {
                setHashtagMentionOnClickListener(ss, range)
            } else if (value.startsWith("@")) {
                setUserMentionOnClickListener(ss, range)
            }

            // set the mention's colour, and font's weight
            val txtColor = ForegroundColorSpan(Color.parseColor("#3686ff"))
            val txtWeight = android.text.style.StyleSpan(android.graphics.Typeface.BOLD)
            ss.setSpan(txtColor, range.first, range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ss.setSpan(txtWeight, range.first, range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return ss
    }

    /**
     * sets the on click listener for user mentions.
     * @param ss the spannable string that contains the user's mention
     * @param range the range of the text that contains the user's mention
     */
    private fun setUserMentionOnClickListener(ss: SpannableString, range: IntRange) {
        val string = ss.toString()
        val username = string.substring(range.first + 1, range.last + 1)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // go the search activity, and pass the username as a parameter
                val intent = Intent(mContext, SearchActivity::class.java)
                intent.putExtra("username", username)
                mContext.startActivity(intent)
            }
        }

        ss.setSpan(
            clickableSpan,
            range.first,
            range.last + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * sets the on click listener for hashtag mentions.
     * @param ss the spannable string that contains the hashtag's mention
     * @param range the range of the text that contains the hashtag's mention
     */
    private fun setHashtagMentionOnClickListener(ss: SpannableString, range: IntRange) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // go the search activity, and pass the hashtag as a parameter
                val string = ss.toString()
                val hashtag = string.substring(range.first, range.last + 1)
                val intent = Intent(mContext, SearchActivity::class.java)
                intent.putExtra("hashtag", hashtag)
                mContext.startActivity(intent)
            }
        }

        ss.setSpan(
            clickableSpan,
            range.first,
            range.last + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * calculate the time difference between now, and when the post date was uploaded.
     *
     * @param datetime the post's upload time in the format "EEE MMM d HH:mm:ss z yyyy"
     *
     * @return the time difference between now, and when the post was uploaded, either in minutes,
     * hours, or days
     */
    fun getTimeDiff(datetime: String): String {
        // setup the time format
        val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z")
        // get the post's upload time as a Date object
        val postDate = sdf.parse(datetime)
        // get the time now
        val now = Calendar.getInstance().time
        // calculate the difference in time
        val diff = now.time - postDate.time

        // calculate the difference in minutes
        val minutesDiff = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS)

        // return it if it's less than 60
        if (minutesDiff < 60) {
            if (minutesDiff.toInt() == 1) {
                return "$minutesDiff minute ago"
            }
            return "$minutesDiff minutes ago"
        }

        // calculate the difference in hours
        val hoursDiff = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)

        // return it if it's less than 24
        if (hoursDiff < 24) {
            if (hoursDiff.toInt() == 1) {
                return "$hoursDiff hour ago"
            }
            return "$hoursDiff hours ago"
        }

        // otherwise, calculate the difference in days and return it
        val daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        if (daysDiff.toInt() == 1) {
            return "$daysDiff day ago"
        }
        return "$daysDiff days ago"
    }
}
