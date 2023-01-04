package com.example.photorama

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.heplerObjects.ImageUriUtils
import com.example.photorama.viewModels.PostViewModel
import com.example.photorama.viewModels.PostViewModelFactory
import kotlinx.android.synthetic.main.activity_upload_post.*

/**
 * @author Sultan
 * handles uploading a post to the server.
 */

class UploadPostActivity : AppCompatActivity() {

    // the post's image
    private lateinit var bitmap: Bitmap

    private lateinit var viewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        // initialize the view model
        val factory = PostViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(PostViewModel::class.java)
        // initialize observers
        initPostUploadObserver()
        initPostUploadErrorObserver()

        onNavigationIconClick()
        showKeyBoard()
        setPreviewImage()

        upload_post_btn.setOnClickListener { uploadPost() }
    }

    /**
     * observes whether uploading a post was successful or not.
     */
    private fun initPostUploadObserver() {
        viewModel.isPostUploaded().observe(this, Observer { isUploaded ->
            if (!isUploaded) {
                return@Observer
            }

            // go back to the main app'a activity
            val intent = Intent(applicationContext, MainAppActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        })
    }

    /**
     * checks if there are any errors when attempting to upload a post.
     */
    private fun initPostUploadErrorObserver() {
        viewModel.getErrorMessage().observe(this, Observer { error ->
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * sets the toolbar's back button's on click listener.
     */
    private fun onNavigationIconClick() {
        tool_bar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * displays the keyboard once this activity is launched
     */
    private fun showKeyBoard() {
        // request focus from the EditText element
        post_description.requestFocus()
        // display the keyboard
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(post_description, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * displays a preview of the image that will be uploaded
     */
    private fun setPreviewImage() {
        // get the image from storage
        val uri = intent.getParcelableExtra("bitmap") as Uri
        // display it on this activity's ImageView
        bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri)
        post_image.setImageBitmap(bitmap)

        // delete the cached image
        ImageUriUtils(this).deleteDirectory()
    }

    /**
     * upload the post to the server.
     */
    private fun uploadPost() {
        // get the bytes from the image file
        val imageByteArray = ImageUriUtils(this).getByteArray()
        // encode the image's byte array into a base64 string
        val base64Image = Base64.encodeToString(imageByteArray, Base64.DEFAULT)

        // get the post's description
        val description = post_description.text.toString()

        // send the post to the server
        viewModel.uploadPost(base64Image, description)
    }
}
