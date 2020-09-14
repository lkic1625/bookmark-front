package com.example.mybook.retrofit

import android.util.Log
import android.widget.Toast
import com.example.mybook.model.MyFeed
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServiceExecutor {
    companion object{

        val ERROR_CODE_RETROFIT = "retrofit_error"
        val NORMAL_CODE_RETROFIT = "retrofit_normal"
        fun like(token:String ,user_id: Int, feed_id: Int, errorCallback: ( response: Response<ResponsePOJO> ) -> Unit = {}, successCallback: (  response: Response<ResponsePOJO> ) -> Unit = {} ){
            val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
            AuthService.like(user_id, feed_id).enqueue(
                object : Callback<ResponsePOJO> {
                    override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                        Log.e(ERROR_CODE_RETROFIT, t.toString())
                    }
                    override fun onResponse(call: Call<ResponsePOJO>, response: Response<ResponsePOJO>) {
                        if (response.code() == 200) {
                            successCallback(response)
                        } else {
                            errorCallback(response)
                        }
                    }
                })
        }

        fun getFeedsByUserId(token: String, user_id: Int, errorCallback: ( response: Response<ResponsePOJO> ) -> Unit = {}, successCallback: (  response: Response<ResponsePOJO> ) -> Unit = {}){
            val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
            AuthService.getFeedsByUserId(user_id).enqueue(
                object : Callback<ResponsePOJO> {
                    override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                        Log.e(ERROR_CODE_RETROFIT, t.toString())
                    }
                    override fun onResponse(call: Call<ResponsePOJO>, response: Response<ResponsePOJO>) {
                        if (response.code() == 200) {
                            successCallback(response)
                        } else {
                            errorCallback(response)
                        }
                    }
                })
        }
    }
}