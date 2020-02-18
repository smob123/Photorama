package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.GetUserNotificationsQuery
import com.example.photorama.R
import com.example.photorama.adapters.NotificationsAdapter
import com.example.photorama.networking.Queries
import kotlinx.android.synthetic.main.notifications_fragment.*

/**
 * @author Sultan
 * notifications fragment
 */

class NotificationsFragment : Fragment() {
    // range of notifications to fetch
    private var notificationsRange = IntRange(0, 20)
    // the amount of increment to increase the range by, in order to fetch more notifications
    private var RANGE_INCREAMENT = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.notifications_fragment, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnRefreshListener()
        getNotifications()
        addOnScrollListener()
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setOnRefreshListener() {
        refresh_layout.setOnRefreshListener {
            notificationsRange = IntRange(0, 20)
            getNotifications()
        }
    }

    /**
     * get post notifications from the server.
     */
    private fun getNotifications() {
        Queries(this@NotificationsFragment.activity!!.applicationContext)
            .getNotifications(notificationsRange.first, notificationsRange.last,
                onCompleted = { err, res ->

                    // stop the refresh layout from refreshing
                    if (refresh_layout.isRefreshing) {
                        this@NotificationsFragment.activity?.runOnUiThread {
                            refresh_layout.isRefreshing = false
                        }
                    }

                    if(err != null) {
                        // display an error message if an error occurs
                        this@NotificationsFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@NotificationsFragment.activity?.applicationContext,
                                "Couldn't retrieve posts from the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@getNotifications
                    }

                    if (res != null) {
                        if(res == null) {
                            // display an error message if an error occurs
                            this@NotificationsFragment.activity?.runOnUiThread {
                                Toast.makeText(
                                    this@NotificationsFragment.activity?.applicationContext,
                                    "Couldn't retrieve posts from the network",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@getNotifications
                        }

                        res.userNotifications?.let { list ->
                            val adapter = notifications_list.adapter
                            // check if the list is empty or not
                            if (list.isNotEmpty()) {
                                // check if the adapter has been initialized, or if the range
                                // starts at 0
                                if (adapter == null || notificationsRange.first == 0) {
                                    // initialize an adapter, and display the notifications
                                    displayNotifications(list)
                                } else {
                                    // otherwise, add the new items to the list
                                    addItems(list)
                                }
                            } else {
                                // otherwise, check if the adapter hasn't been initialized, or
                                // if it's empty
                                if (notifications_list.adapter == null || adapter!!.itemCount == 0) {
                                    // display the text view
                                    displayMessage()
                                } else {
                                    // otherwise, remove the scroll listener from the recycler view
                                    this@NotificationsFragment.activity?.let { activity ->
                                        activity.runOnUiThread {
                                            notifications_list.clearOnScrollListeners()
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
    }

    /**
     * initializes a new adapter for the recycler view.
     * @param notifications the list of notifications to add to the adapter
     */
    private fun displayNotifications(notifications: List<GetUserNotificationsQuery.GetUserNotification>) {
        // store the notifications in an list, to pass it to the adapter
        val notificationsList =
            ArrayList<GetUserNotificationsQuery.GetUserNotification>(notifications)
        this@NotificationsFragment.activity?.runOnUiThread {
            // update the recycler view's, and text view's visibility
            notifications_list.visibility = View.VISIBLE
            no_notifications_text.visibility = View.GONE

            // initialize the recycler view's layout manager, and adapter
            val adapter =
                NotificationsAdapter(
                    this@NotificationsFragment.activity!!,
                    notificationsList
                )
            val manager =
                LinearLayoutManager(this@NotificationsFragment.activity!!.applicationContext)
            notifications_list.adapter = adapter
            notifications_list.layoutManager = manager
        }
    }

    /**
     * display a message telling the user that there are no notifications.
     */
    private fun displayMessage() {
        this@NotificationsFragment.activity?.let {
            this@NotificationsFragment.activity!!.runOnUiThread {
                notifications_list.visibility = View.GONE
                no_notifications_text.visibility = View.VISIBLE
            }
        }
    }

    /**
     * sets the recycler view's on scroll listener.
     */
    private fun addOnScrollListener() {
        notifications_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isScrolling = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = notifications_list.layoutManager as LinearLayoutManager
                val visibleItems = manager.childCount
                val totalNumOfItems = manager.itemCount
                val scrolledOutItems = manager.findFirstVisibleItemPosition()

                if (isScrolling && (visibleItems + scrolledOutItems) == totalNumOfItems) {
                    isScrolling = false
                    notificationsRange = IntRange(
                        notificationsRange.first + RANGE_INCREAMENT,
                        notificationsRange.last + RANGE_INCREAMENT
                    )

                    getNotifications()
                }
            }
        })
    }

    /**
     * adds more posts to the recycler view.
     * @param notifications the list of notifications to add to the view
     */
    private fun addItems(notifications: List<GetUserNotificationsQuery.GetUserNotification>) {
        val newItems = ArrayList<GetUserNotificationsQuery.GetUserNotification>(notifications)

        this@NotificationsFragment.activity?.let { activity ->
            activity.runOnUiThread {
                val adapter = notifications_list.adapter as NotificationsAdapter
                adapter.addItems(newItems)
            }
        }
    }
}
