package com.example.mybook.model

import android.os.Parcel
import android.os.Parcelable

data class MyFeed (var no:Int = 0,
                   var imgsrc:String = "",
                   var bname:String = "",
                   var author:String= "",
                   var publisher:String="",
                   var contents:String="",
                   var like:Int = 0,
                   var imageLink:String="",
                   var isbn:String ="",
                   var date:String="",
                   var user_id:Int = -1):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(no)
        parcel.writeString(imgsrc)
        parcel.writeString(bname)
        parcel.writeString(author)
        parcel.writeString(publisher)
        parcel.writeString(contents)
        parcel.writeInt(like)
        parcel.writeString(imageLink)
        parcel.writeString(isbn)
        parcel.writeString(date)
        parcel.writeInt(user_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyFeed> {
        override fun createFromParcel(parcel: Parcel): MyFeed {
            return MyFeed(parcel)
        }

        override fun newArray(size: Int): Array<MyFeed?> {
            return arrayOfNulls(size)
        }
    }

}