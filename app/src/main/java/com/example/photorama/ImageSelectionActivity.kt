package com.example.photorama

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.photorama.heplerObjects.ImageUriUtils
import kotlinx.android.synthetic.main.activity_image_selection.*

/**
 * @author Sultan
 * handles selecting a source to get images from; either camera, or gallery.
 */

class ImageSelectionActivity : AppCompatActivity() {

    // the type of the image that we want to upload
    enum class ImageType {
        POST, AVATAR
    }

    // get activity results codes
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY_IMAGE = 2
    private var imageType: ImageType = ImageType.POST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)

        // add a back button to the action bar, and set the bar's title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Image Selection"

        // get the image type from the previous activity
        val bundle = intent.extras
        imageType = bundle!!.get("image type") as ImageType

        // set on click listeners for both views in the activity
        setOnClickListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return false
    }

    /**
     * sets onClick listeners for both view in the activity.
     */
    private fun setOnClickListeners() {
        use_camera_view.setOnClickListener {
            // check if permissions were granted
            val permissions =
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

            if (hasPermissions(permissions)) {
                startCameraActivity()
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_IMAGE_CAPTURE)
            }
        }

        use_gallery_view.setOnClickListener {
            // check if permissions were granted
            val permissions =
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            if (hasPermissions(permissions)) {
                getImageFromGallery()
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_GALLERY_IMAGE)
            }
        }
    }

    /**
     * checks if permissions are granted.
     * @param permissions the list of permissions to check
     * @return whether the permissions was granted or not.
     */
    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    /**
     * runs the default camera activity.
     */
    private fun startCameraActivity() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val imageUriUtils = ImageUriUtils(this)
        imageUriUtils.createFile()
        val uri = imageUriUtils.getTempImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    /**
     * asks the user to select an image from the gallery.
     */
    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // initialize the next activity's intent
        val intent =
            Intent(this@ImageSelectionActivity.applicationContext, ImageCropperActivity::class.java)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        var uri: Uri? = null

        // check if the activity result is coming from the camera, or the gallery, and store
        // the result
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode != Activity.RESULT_CANCELED) {
            uri = ImageUriUtils(this).getTempImageUri()
        } else if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data?.data != null) {
                uri = data.data
            }
        }

        // make sure that uri is not null
        if (uri != null) {
            // put the image's type as an extra for the next activity
            if (imageType == ImageType.AVATAR) {
                intent.putExtra("post type", ImageCropperActivity.ImageType.AVATAR)
            } else if (imageType == ImageType.POST) {
                intent.putExtra("post type", ImageCropperActivity.ImageType.POST)
            }

            // put the image's uri as an extra
            intent.putExtra("bitmap", uri)
            startActivity(intent)
        }
    }
}
