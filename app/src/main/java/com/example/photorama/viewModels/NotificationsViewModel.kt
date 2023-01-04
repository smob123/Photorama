package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.NotificationType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.NotificationsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsViewModel(private val context: Context) : ViewModel() {
    // stores the user's notifications
    private var mNotifications =
        MutableLiveData<ArrayList<NotificationType>>()

    private val notificationsFetchErrorMessage = MutableLiveData<ErrorMessage>()

    // checks whether a network request is being made or not
    private var isFetching = MutableLiveData<Boolean>()

    // a repository object that is responsible for making network requests
    private val notificationRepo: NotificationsRepo

    /**
     * initialize the global variables.
     */
    init {
        notificationRepo = NotificationsRepo.getInstance(context)
    }

    /**
     * returns the stored notifications.
     */
    fun getNotifications() = mNotifications as LiveData<ArrayList<NotificationType>>

    /**
     * returns error messages that were sent from the server.
     */
    fun getNotificationsFetchErrorMessage() =
        notificationsFetchErrorMessage as LiveData<ErrorMessage>

    /**
     * return whether a network request is being made or not.
     */
    fun isFetching() = isFetching as LiveData<Boolean>

    /**
     * fetches, and returns notifications.
     */
    fun fetchNotifications(startIndex: Int, endIndex: Int, checkCache: Boolean) {
        isFetching.value = true

        // get cached posts first
        if (checkCache) {
            mNotifications.value = notificationRepo.getCache()
        }

        // try to fetch newer notifications from the network
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = notificationRepo.fetchNotifications(startIndex, endIndex)

            if (fetchedData == null) {
                notificationsFetchErrorMessage.postValue(notificationRepo.notificationsFetchErrorMessage)
                return@launch
            }

            mNotifications.postValue(fetchedData)
            isFetching.postValue(false)
        }
    }
}
