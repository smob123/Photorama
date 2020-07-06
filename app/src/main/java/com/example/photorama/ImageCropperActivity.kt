package com.example.photorama

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.custom_ui.MCanvas
import com.example.photorama.heplerObjects.ImageUriUtils
import com.example.photorama.viewModels.UserProfileViewModel
import com.example.photorama.viewModels.UserProfileViewModelFactory
import kotlinx.android.synthetic.main.activity_image_cropper.*
import java.io.ByteArrayOutputStream

/**
 * @author Sultan
 * handles cropping images based on their type.
 */
class ImageCropperActivity : AppCompatActivity() {
    // type of image to send to othe server
    enum class ImageType {
        POST, AVATAR
    }

    private lateinit var mCanvas: MCanvas

    private lateinit var viewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_cropper)

        // get the image's uri to display it, and its type
        val uri = intent.getParcelableExtra("bitmap") as Uri
        val bundle = intent.extras

        val postType = bundle!!.get("post type") as ImageType

        // set the on click listener for the toolbar's buttons
        onNavigationIconClick()
        setToolbar(postType)

        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        if (bitmap != null) {
            if (postType == ImageType.AVATAR) {
                setCanvas(bitmap, 500, 500)
                val factory = UserProfileViewModelFactory(this)
                viewModel = ViewModelProvider(this, factory).get(UserProfileViewModel::class.java)
                initUploadAvatarObserver()
                initUploadAvatarErrorObserver()
            } else if (postType == ImageType.POST) {
                setCanvas(bitmap, 1080, 1080)
            }
        } else {
            finish()
        }
    }

    private fun initUploadAvatarObserver() {
        viewModel.isAvatarUploaded().observe(this, Observer { isUploaded ->
            if (isUploaded) {
                // go back to the main app'a activity
                val intent = Intent(applicationContext, MainAppActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        })
    }

    private fun initUploadAvatarErrorObserver() {
        viewModel.getErrorMessage().observe(this, Observer { error ->
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * sets the toolbar's back button listener.
     */
    private fun onNavigationIconClick() {
        tool_bar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * adds the image to the canvas, and displays it on the view.
     * @param bitmap the image to display on the canvas
     * @param width the desired output's width
     * @param height the desired output's height
     */
    private fun setCanvas(bitmap: Bitmap, width: Int, height: Int) {
        mCanvas = MCanvas(
            this,
            null,
            bitmap,
            width,
            height
        )
        canvas.addView(mCanvas)
    }

    /**
     * sets the "next stage" button's on click listener.
     * @param postType the type of the image; whether it's a post image, or an avatar image
     */
    private fun setToolbar(postType: ImageType) {
        // set the button's text based on the type of the image
        val buttonText = if (postType == ImageType.AVATAR) "Upload" else "Next"
        next_stage_btn.text = buttonText

        next_stage_btn.setOnClickListener {
            // get the bitmap from the canvas, and encode it into a byte array
            val bitmap = mCanvas.cropBitmap()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            if (postType == ImageType.AVATAR) {
                // if the image type is an avatar image, send the image to the server
                uploadProfileImage(byteArray)
            } else if (postType == ImageType.POST) {
                // otherwise go to the next activity to upload it as a post
                goToNextActivity(byteArray)
            }
        }
    }

    /**
     * goes the the upload post activity.
     * @param byteArray the cropped image as a byte array
     */
    private fun goToNextActivity(byteArray: ByteArray) {
        val imageUriUtils = ImageUriUtils(this)
        // remove the existing file, and recreate it
        imageUriUtils.createFile()
        // save the new image in the file
        imageUriUtils.writeToFile(byteArray)

        val intent = Intent(this, UploadPostActivity::class.java)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra("bitmap", imageUriUtils.getTempImageUri())
        startActivity(intent)
    }

    /**
     * uploads the image to the server to replace the current avatar.
     * @param imageBytes the image to upload as a ByteArray
     */
    private fun uploadProfileImage(imageBytes: ByteArray) {
        // get the query's required parameters from the cache
        val base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        // execute the query
        viewModel.uploadAvatar(base64)
    }
}
