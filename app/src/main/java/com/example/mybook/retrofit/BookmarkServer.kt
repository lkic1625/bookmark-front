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

    @POST("v1/board/feeds")
    @FormUrlEncoded
    fun uploadFeed(
        @Field("user_id") user_id: Int,
        @Field("feed_author") author: String,
        @Field("feed_contents") contents: String,
        @Field("feed_imgUri") imgUri: String,
        @Field("book_author") book_author: String,
        @Field("book_name") book_name:String,
        @Field("book_isbn") book_isbn:String,
        @Field("book_publisher") book_publisher:String
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