package com.example.mybook.model

import java.io.Serializable

data class Book(
    val title:String ="",
    val author:String ="",
    val price:Int = 0,
    val discount:Int = 0,
    val publisher:String = "",
    val pubdate:String = "",
    val isbn:String = "",
    val link:String = "",
    val description:String = "",
    val imageLink:String = "",
    val timeRead:String = ""
):Serializable{
    init {
        var btitle = title
        var bauthor = author
        var bprice = price
        var bdiscount = discount
        var  bpubli = publisher
        var bpub = pubdate
        var  bisbn = isbn
        var blink = link
        var bdes=description
        var bimg = imageLink
    }
}