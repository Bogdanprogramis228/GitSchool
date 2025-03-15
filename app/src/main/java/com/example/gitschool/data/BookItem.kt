package com.example.gitschool.data

import android.os.Parcel
import android.os.Parcelable

data class BookItem(
    val title: String,
    val description: String,
    val author: String,
    val year: String,
    val pdfUrl: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(author)
        parcel.writeString(year)
        parcel.writeString(pdfUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<BookItem> {
            override fun createFromParcel(parcel: Parcel): BookItem {
                return BookItem(parcel)
            }

            override fun newArray(size: Int): Array<BookItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
