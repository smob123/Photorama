package com.example.photorama.networking

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.bumptech.glide.Glide

/**
 * clears Glide's disk cache.
 */
class ClearImageCache(private val context: Context) :
    AsyncTask<String, Void, Unit>() {
    override fun doInBackground(vararg p0: String?) {
        Glide.get(context).clearDiskCache()
    }
}
