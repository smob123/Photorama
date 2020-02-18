package com.example.photorama.heplerObjects

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @author Sultan
 * handles storing images temporarily in the system, and returning their Uris.
 */

class ImageUriUtils(private val context: Context) {
    // the directory to store the image in
    private val dir: File
    // the of the file we want to write the image's data into
    private val file: File

    init {
        dir = File(Environment.getExternalStorageDirectory(), "photorama_images")
        file = File(dir, "tempImage")
    }

    /**
     * creates a file to store the image in.
     */
    fun createFile() {
        if (!dir.exists()) {
            dir.mkdir()
        }

        if (file.exists()) {
            file.delete()
        }

        file.createNewFile()
    }

    /**
     * deletes the created file.
     */
    fun deleteDirectory() {
        if (dir.exists()) {
            dir.delete()
        }
    }

    /**
     * writes image's byte array into the file.
     * @param byteArray the image's data as a byte array
     * @return whether writing to the file was successful or not
     */
    fun writeToFile(byteArray: ByteArray): Boolean {
        // check if the file exists
        if (!file.exists()) {
            return false
        }

        // write the image's data into the file
        val stream = BufferedOutputStream(FileOutputStream(file))
        stream.write(byteArray)
        stream.flush()
        stream.close()
        return true
    }

    /**
     * get the byte array from the file.
     * @return the byte array from the file
     */
    fun getByteArray(): ByteArray {
        val stream = FileInputStream(file)
        return stream.readBytes()
    }

    /**
     * returns the Uri of the file.
     * @return the image file's Uri
     */
    fun getTempImageUri(): Uri? {
        // check if the directory, or the file don't exist
        if (!dir.exists() || !file.exists()) {
            return null
        }

        // otherwise return the file's Uri
        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
    }
}
