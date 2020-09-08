package com.example.mybook.retrofit

import com.example.mybook.MyCatg
import retrofit2.Call
import retrofit2.http.*

interface BookmarkServer {
    @POST("v1/user/account/signup")
    @FormUrlEncoded
    fun signUp(
        @Field("user_email") email:String,
        @Field("user_pw") pw:String,
        @Field("user_name") name:String,
        @Field("categories") category:List<MyCatg>
    ): Call<ResponsePOJO>

    @POST("v1/user/account/auth")
    @FormUrlEncoded
    fun auth(
        @Field("user_email") email: String,
        @Field("user_pw") pw: String
    ): Call<ResponsePOJO>

    @GET("v1/board/categories/user/{user_id}")
    fun getCategoryById(
        @Path("user_id") id:Int
    ): Call<ResponsePOJO>

    @GET("v1/board/feeds/{feed_id}")
    fun getFeedsById(
        @Path("feed_id") id:Int
    ): Call<ResponsePOJO>

    @GET("v1/board/feeds/user/{user_id}")
    fun getFeedsByUserId(
        @Path("user_id") id:Int
    ): Call<ResponsePOJO>


    @GET("v1/board/books/{book_id}")
    fun getBooksById(
        @Path("book_id") id:Int
    ): Call<ResponsePOJO>

    @GET("v1/board/books/user/{user_id}")
    fun getBooksByUserId(
        @Path("user_id") id:Int
    ): Call<ResponsePOJO>



}