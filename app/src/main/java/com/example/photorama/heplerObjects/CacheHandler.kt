package com.example.photorama.heplerObjects

import android.content.Context
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

/**
 * @author Sultan
 * @param context the calling activity's context
 * This class is responsible for handling storing, and retrieving cache.
 */
class CacheHandler(private val context: Context) {
    private val cacheDir: File // the app's cache directory
    private val cacheFile: File // the cache file

    // cache file to temporarily store images to user them in different activities
    private val cacheFileName = "photorama.temp" // the name of the cache file
    private val timelineCacheFile: File
    private val notificationsCacheFile: File
    private val profilePostsCacheFile: File

    /**
     * constructor.
     */
    init {
        // initialize the cache directories, and files
        cacheDir = context.cacheDir
        cacheFile = File(cacheDir, cacheFileName)
        timelineCacheFile = File(cacheDir, "timeline_cache.temp")
        notificationsCacheFile = File(cacheDir, "notifications_cache.temp")
        profilePostsCacheFile = File(cacheDir, "profile_posts_cache.temp")
    }

    /**
     * overwrites the existing cache file.
     * @param data the data we want to cache
     */
    fun storeLoginCache(data: JSONObject) {
        // check if the file exists
        if (cacheFile.exists()) {
            // delete the file, and create a new one
            cacheFile.delete()
            cacheFile.createNewFile()
        }

        // initialize a BufferedWriter with to write the data into the cache file
        val bw = BufferedWriter(FileWriter(cacheFile.path))
        bw.write(data.toString())
        bw.flush()
        bw.close()
    }

    /*
     * reads the content of the cache file, and returns it.
     * @return the cached data as a JSONObject
     */
    fun getCache(): JSONObject {
        // if the file does not exist, then return an empty object
        if (!cacheFile.exists()) {
            return JSONObject()
        }

        // read the content of the file
        val scan = Scanner(cacheFile)
        var data = ""

        while (scan.hasNext()) {
            data += scan.nextLine()
        }

        // try to cast the data into a JSONObject, and return it
        var jsonObject: JSONObject
        try {
            jsonObject = JSONObject(data)
        } catch (err: JSONException) {
            jsonObject = JSONObject()
        }

        return jsonObject
    }

    fun storeTimelineCache(data: JSONArray) {
        storeCache(data, timelineCacheFile)
    }

    fun getTimelineCache(): JSONArray {
        return getCache(timelineCacheFile)
    }

    fun storeNotificationsCache(data: JSONArray) {
        storeCache(data, notificationsCacheFile)
    }

    fun getNotificationsCache(): JSONArray {
        return getCache(notificationsCacheFile)
    }

    fun storeProfilePostsCache(data: JSONArray) {
        storeCache(data, profilePostsCacheFile)
    }

    fun getProfilePostsCache(): JSONArray {
        return getCache(profilePostsCacheFile)
    }

    private fun storeCache(data: JSONArray, file: File) {
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }

        // initialize a BufferedWriter with to write the data into the cache file
        val bw = BufferedWriter(FileWriter(file.path))
        bw.write(data.toString())
        bw.flush()
        bw.close()
    }

    private fun getCache(file: File): JSONArray {
        // if the file does not exist, then return an empty object
        if (!file.exists()) {
            return JSONArray()
        }

        // read the content of the file
        val scan = Scanner(file)
        var data = ""

        while (scan.hasNext()) {
            data += scan.nextLine()
        }

        // try to cast the data into a JSONObject, and return it
        var jsonArray: JSONArray
        try {
            jsonArray = JSONArray(data)
        } catch (err: JSONException) {
            jsonArray = JSONArray()
        }

        return jsonArray
    }

    /**
     * deletes the cache directory
     */
    fun deleteCacheDir() {
        for (file in cacheDir.listFiles()) {
            file.delete()
        }
        cacheDir.delete()

        CoroutineScope(Dispatchers.IO).launch {
            Glide.get(context).clearDiskCache()
        }
    }
}
