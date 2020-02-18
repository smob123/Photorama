package com.example.photorama.heplerObjects

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull

/**
 * @author Sultan
 * a custom parcelable object that's used to store a post's info, and pass them between activities.
 */

class PostParcelable(
    @NonNull val userId: String, @NonNull val username: String, @NonNull val userScreenName: String,
    @NonNull val postId: String, val userAvatar: String, @NonNull val likes: List<String>,
    @NonNull val comments: List<String>, @NonNull val datetime: String,
    @NonNull val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(username)
        parcel.writeString(userScreenName)
        parcel.writeString(postId)
        parcel.writeString(userAvatar)
        parcel.writeStringArray(likes.toTypedArray())
        parcel.writeStringArray(comments.toTypedArray())
        parcel.writeString(datetime)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostParcelable> {
        override fun createFromParcel(parcel: Parcel): PostParcelable {
            return PostParcelable(parcel)
        }

        override fun newArray(size: Int): Array<PostParcelable?> {
            return arrayOfNulls(size)
        }
    }
}