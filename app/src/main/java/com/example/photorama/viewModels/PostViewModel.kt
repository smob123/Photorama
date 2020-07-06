package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.CommentType
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.PostRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostViewModel(private val context: Context) :
    ViewModel() {
    private val postInfo = MutableLiveData<PostType>()

    private val comments = MutableLiveData<ArrayList<CommentType>>()

    private val likes = MutableLiveData<ArrayList<String>>()

    private val postUploaded = MutableLiveData<Boolean>()

    private val errorMessage = MutableLiveData<ErrorMessage>()

    private val isFetching = MutableLiveData<Boolean>()

    private val repo: PostRepo

    init {
        repo = PostRepo.getInstance(context)
    }

    fun getPostInfo() = postInfo as LiveData<PostType?>

    fun getLikes() = likes as LiveData<ArrayList<String>>

    fun getComments() = comments as LiveData<ArrayList<CommentType>>

    fun isFetching() = isFetching as LiveData<Boolean>

    fun isPostUploaded() = postUploaded as LiveData<Boolean>

    fun getErrorMessage() = errorMessage as LiveData<ErrorMessage>

    /**
     * sets the value of postInfo.
     */
    fun setPostInfo(postType: PostType) {
        postInfo.value = postType
        likes.value = ArrayList(postType.likes)
    }

    /**
     * fetches the post's info from the server.
     */
    fun fetchPostInfo(postId: String) {
        isFetching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val info = repo.fetchPostInfo(postId)

            if (info == null) {
                errorMessage.postValue(repo.errorMessage)
                return@launch
            }

            // update postInfo's value
            postInfo.postValue(info)
            // update the list of likes
            likes.postValue(ArrayList(info.likes))
            isFetching.postValue(false)
        }
    }

    /**
     * fetches the post's comments.
     */
    fun fetchComments(postId: String, startIndex: Int, endIndex: Int) {
        isFetching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val postComments = repo.fetchPostComments(postId, startIndex, endIndex)

            if (postComments == null) {
                errorMessage.postValue(repo.errorMessage)
            }

            comments.postValue(postComments)
            isFetching.postValue(false)
        }
    }

    /**
     * posts a new comment to the server.
     */
    fun sendComment(postId: String, comment: String) {
        isFetching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newComment = repo.sendComment(postId, comment)!!
                val commentList = comments.value ?: ArrayList()
                commentList.add(0, newComment)
                comments.postValue(commentList)
                isFetching.postValue(false)
            } catch (e: Exception) {
                errorMessage.postValue(repo.errorMessage)
                isFetching.postValue(false)
            }
        }
    }

    /**
     * deletes a comment.
     */
    fun deleteComment(commentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val deleted = repo.removeComment(commentId)
            // check if the request was successful
            if (!deleted) {
                errorMessage.postValue(repo.errorMessage)
                return@launch
            }

            val commentList = comments.value!!
            for (comment in commentList) {
                if (comment.commentId == commentId) {
                    commentList.remove(comment)
                }
            }

            comments.postValue(commentList)
        }
    }

    /**
     * sends a "like post" request to the server.
     */
    fun likePost(postId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isLiked = repo.likePost(postId)

            // check if the request was successful
            if (!isLiked) {
                errorMessage.postValue(repo.errorMessage)
                return@launch
            }

            val json = CacheHandler(context).getCache()
            val id = json.getString("id")
            val likesList = postInfo.value!!.likes as ArrayList
            likesList.add(id)
            likes.postValue(likesList)
        }
    }

    /**
     * sends a "unlike post" request to the server.
     */
    fun unlikePost(postId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isUnliked = repo.unlikePost(postId)
            // check if the request was successful
            if (!isUnliked) {
                errorMessage.postValue(repo.errorMessage)
                return@launch
            }

            val json = CacheHandler(context).getCache()
            val id = json.getString("id")
            val likesList = postInfo.value!!.likes as ArrayList
            likesList.remove(id)
            likes.postValue(likesList)
        }
    }

    /**
     * sends a "delete post" request to the server.
     */
    fun deletePost(postId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val isDeleted = repo.deletePost(postId)
            // check if the request was successful
            if (!isDeleted) {
                errorMessage.postValue(repo.errorMessage)
                return@launch
            }

            // update the user's timeline, and profile posts
            val timelineViewModelFactory = TimelineViewModelFactory(context)
            val timelineViewModel =
                ViewModelProvider(context as ViewModelStoreOwner, timelineViewModelFactory).get(
                    TimelineViewModel::class.java
                )

            val profileViewModelFactory = UserProfileViewModelFactory(context)
            val profileViewModel =
                ViewModelProvider(context as ViewModelStoreOwner, profileViewModelFactory).get(
                    UserProfileViewModel::class.java
                )

            withContext(Dispatchers.Main) {
                timelineViewModel.deletePost(postId)
                profileViewModel.deletePost(postId)
            }

            // nullify the post's info
            postInfo.postValue(null)
        }
    }

    fun uploadPost(base64Image: String, description: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val uploadPost = repo.uploadPost(base64Image, description)

            if (!uploadPost) {
                errorMessage.postValue(repo.errorMessage)
            } else {
                postUploaded.postValue(uploadPost)
            }
        }
    }
}
