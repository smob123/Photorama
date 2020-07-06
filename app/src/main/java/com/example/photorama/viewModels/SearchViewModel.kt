package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.HashtagType
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.SearchRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(private val context: Context) : ViewModel() {
    private val users = MutableLiveData<ArrayList<UserType>>()

    private val usersSearchedById = MutableLiveData<ArrayList<UserListItemType>>()

    private val hashtags = MutableLiveData<ArrayList<HashtagType>>()

    private val posts = MutableLiveData<ArrayList<PostType>>()

    private val repo: SearchRepo

    private val hashtagSearchErrorMessage = MutableLiveData<ErrorMessage>()

    private val userSearchErrorMessage = MutableLiveData<ErrorMessage>()

    private val postSearchErrorMessage = MutableLiveData<ErrorMessage>()

    fun getUsers() = users as LiveData<ArrayList<UserType>>

    fun getUsersSearchedById() = usersSearchedById as LiveData<ArrayList<UserListItemType>>

    fun getHashtags() = hashtags as LiveData<ArrayList<HashtagType>>

    fun getPosts() = posts as LiveData<ArrayList<PostType>>

    fun getHashtagSearchErrorMessage() = hashtagSearchErrorMessage as LiveData<ErrorMessage>

    fun getUserSearchErrorMessage() = userSearchErrorMessage as LiveData<ErrorMessage>

    fun getPostSearchErrorMessage() = postSearchErrorMessage as LiveData<ErrorMessage>

    init {
        repo = SearchRepo.getInstance(context)
    }

    fun searchUsers(username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = repo.searchUserByName(username)

            if (fetchedData == null) {
                userSearchErrorMessage.postValue(repo.userSearchErrorMessage)
                return@launch
            }

            users.postValue(fetchedData)
        }
    }

    fun searchHashtags(hashtag: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = repo.searchHashtag(hashtag)

            if (fetchedData == null) {
                hashtagSearchErrorMessage.value = repo.hashtagSearchErrorMessage
                return@launch
            }

            hashtags.postValue(fetchedData)
        }
    }

    fun searchPostsByHashtag(hashtag: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = repo.searchPostsByHashtag(hashtag)

            if (fetchedData == null) {
                postSearchErrorMessage.value = repo.hashtagSearchErrorMessage
                return@launch
            }

            posts.postValue(fetchedData)
        }
    }

    fun searchUsersById(userIds: ArrayList<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val userList = repo.searchUsersById(userIds)

            if (userList == null) {
                userSearchErrorMessage.postValue(repo.userByIdSearchErrorMessage)
                return@launch
            }

            usersSearchedById.postValue(userList)
        }
    }
}
