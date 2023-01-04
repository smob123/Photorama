package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.R
import com.example.photorama.adapters.NotificationsAdapter
import com.example.photorama.viewModels.NotificationsViewModel
import com.example.photorama.viewModels.NotificationsViewModelFactory
import kotlinx.android.synthetic.main.notifications_fragment.*

/**
 * @author Sultan
 * notifications fragment
 */

class NotificationsFragment : Fragment() {
    // range of notifications to fetch
    private var notificationsRange = IntRange(0, 20)

    // the amount of increment to increase the range by, in order to fetch more notifications
    private var rangeIncrement = 20

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var notificationsListAdapter: NotificationsAdapter

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

        // initialize the view model
        val factory = NotificationsViewModelFactory(requireActivity())
        notificationsViewModel = ViewModelProvider(
            requireActivity(),
            factory
        ).get(NotificationsViewModel::class.java)

        // initialize UI elements
        initRecyclerView()
        initLoadingViewModel()
        // initialize listeners
        setOnRefreshListener()
        // intialize the view model's observer
        initNotificationsViewModel()
        initNetworkErrorObserver()

        // fetch the first few notifications
        notificationsViewModel.fetchNotifications(
            notificationsRange.first,
            notificationsRange.last,
            true
        )
    }

    /**
     * initializes the data fetching observer, which is used to display/hide the progress bar.
     */
    private fun initLoadingViewModel() {
        notificationsViewModel.isFetching()
            .observe(requireActivity(), Observer { isFetching ->
                refresh_layout.isRefreshing = isFetching!!
            })
    }

    private fun initNetworkErrorObserver() {
        notificationsViewModel.getNotificationsFetchErrorMessage()
            .observe(requireActivity(), Observer { error ->
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show()
            })
    }

    /**
     * initializes the view model's observer, which is used to update the recycler view's content.
     */
    private fun initNotificationsViewModel() {
        notificationsViewModel.getNotifications()
            .observe(requireActivity(), Observer { posts ->
                // add to/update the recycler view's adapter
                if (notificationsRange.first == 0) {
                    notificationsListAdapter.setItems(posts)
                } else {
                    notificationsListAdapter.addItems(posts)
                }

                notificationsListAdapter.notifyDataSetChanged()

                // show/hide the recycler view based on whether the adapter is empty of not
                if (notificationsListAdapter.itemCount > 0) {
                    notifications_list.visibility = View.VISIBLE
                    no_notifications_text.visibility = View.GONE
                } else {
                    notifications_list.visibility = View.GONE
                    no_notifications_text.visibility = View.VISIBLE
                }
            }
            )
    }

    /**
     * initializes the recycler view that displays the list of notification.
     */
    private fun initRecyclerView() {
        notificationsListAdapter = NotificationsAdapter(
            requireActivity(),
            ArrayList()
        )

        notifications_list.layoutManager = LinearLayoutManager(requireActivity().applicationContext)
        notifications_list.adapter = notificationsListAdapter
        addOnScrollListener()
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setOnRefreshListener() {
        refresh_layout.setOnRefreshListener {
            notificationsRange = IntRange(0, 20)
            notificationsViewModel.fetchNotifications(
                notificationsRange.first,
                notificationsRange.last,
                false
            )
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
                        notificationsRange.first + rangeIncrement,
                        notificationsRange.last + rangeIncrement
                    )

                    notificationsViewModel.fetchNotifications(
                        notificationsRange.first,
                        notificationsRange.last,
                        false
                    )
                }
            }
        })
    }
}
