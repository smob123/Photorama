package com.example.photorama.heplerObjects

import android.content.Context
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
class CacheHandler(context: Context) {
    private val cacheDir: File // the app's cache directory
    private val cacheFile: File // the cache file
    // cache file to temporarily store images to user them in different activities
    private val cacheFileName = "photorama.temp" // the name of the cache file

    /**
     * constructor.
     */
    init {
        // initialize the cache directory, and cache file
        cacheDir = context.cacheDir
        cacheFile = File(cacheDir, cacheFileName)
    }

    /**
     * overwrites the existing cache file.
     * @param data the data we want to cache
     */
    fun overWriteCache(data: JSONObject) {
        // check if the file exists
        if (cacheFile.exists()) {
            // delete the file, and create a new one
            cacheFile.delete()
            cacheFile.createNewFile()
        }

        // initialize a BufferedWriter with a non-null file to write the data into the file
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

    /**
     * deletes the cache directory
     */
    fun deleteCacheDir() {
        if (cacheFile.exists()) {
            cacheFile.delete()
            cacheDir.delete()
        }
    }
}