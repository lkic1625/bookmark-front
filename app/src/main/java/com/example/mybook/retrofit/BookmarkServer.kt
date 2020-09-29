package com.example.mybook.retrofit

import com.example.mybook.model.MyCatg
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BookmarkServer {

    @Multipart
    @POST("v1/user/account/profile")
    fun uploadProfileImage(
        @Part image:MultipartBody.Part,
        @Part("user_id") user_id:Int
    ): Call<ResponsePOJO>

    @POST("v1/user/account/signup")
    @FormUrlEncoded
    fun signUp(
        @Field("user_email") email:String,
        @Field("user_pw") pw:String,
        @Field("user_name") name:String,
        @Field("categories") category:HashSet<MyCatg>
    ): Call<ResponsePOJO>

    @POST("v1/user/account/auth")
    @FormUrlEncoded
    fun auth(
        @Field("user_email") email: String,
        @Field("user_pw") pw: String
    ): Call<ResponsePOJO>



    @POST("v1/board/feeds")
    @Multipart
    fun uploadFeed(
        @Part image:MultipartBody.Part,
        @Part("user_id") user_id: Int,
        @Part("feed_author") author: String,
        @Part("feed_contents") contents: String,
        @Part("feed_imgUri") imgUri: String,
        @Part("book_author") book_author: String,
        @Part("book_name") book_name:String,
        @Part("book_isbn") book_isbn:String,
        @Part("book_publisher") book_publisher:String,
        @Part("book_imageLink") book_imageLink:String
        ): Call<ResponsePOJO>

    @POST("v1/board/feeds/like")
    @FormUrlEncoded
    fun like(
       @Field("user_id") user_id: Int,
       @Field("feed_id") feed_id: Int
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

    @GET("v1/board/feeds/{book_id}")
    fun getFeedsByBookId(
        @Path("book_id") id:Int
    ): Call<ResponsePOJO>

    @GET("v1/board/books/user/{user_id}")
    fun getBooksByUserId(
        @Path("user_id") id:Int
    ): Call<ResponsePOJO>







}