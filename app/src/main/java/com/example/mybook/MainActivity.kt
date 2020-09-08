package com.example.mybook



import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.mybook.retrofit.*
import com.example.mybook.security.Hashing
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.Utility.getPackageInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {//로그인 액티비티


    var loggedInUserId:Int = -1
    var token = ""
    val ERROR_CODE_RETROFIT = "retrofit"
    lateinit var REST_API:BookmarkServer
    lateinit var user:ArrayList<User>

    lateinit var user_map:MutableMap<Int,User>
    lateinit var feed:ArrayList<MyFeed>
    lateinit var letter:ArrayList<MyFeed>//글귀
    lateinit var category:ArrayList<MyCatg>
    lateinit var allwant:ArrayList<MyFeed>
    lateinit var db: FirebaseFirestore
    lateinit var tmp:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    fun init(){
        REST_API = RetrofitUtility.getServer()
        //set on click listener
        enroll_page.setOnClickListener {
            clickJoin()
        }
        login_btn.setOnClickListener {
            clickLogin()
        }
    }

     fun clickLogin(){//login button onClick fun
         val user_email = edit_email.text.toString()//User-Entered Email
         val hashed_user_pw = Hashing.calculateHash(edit_pw.text.toString())//Hashed password
         //login API call
         REST_API.auth(
             user_email,
             hashed_user_pw
         ).enqueue(object :Callback<ResponsePOJO> {
             override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                 Log.e(ERROR_CODE_RETROFIT, t.toString())
                 Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
             }

             override fun onResponse(call: Call<ResponsePOJO>, response: Response<ResponsePOJO>) {
                 if (response.code() == 200) {
                     val res = response.body()
                     Toast.makeText(applicationContext, "로그인", Toast.LENGTH_SHORT).show()
                     if (res != null) {
                         loggedInUserId = res.payload.toInt()
                         token = res.token
                         Log.v("retrofit", res.toString() + "login complete")

                     } else {
                         Log.e(ERROR_CODE_RETROFIT, "response body is null")
                     }
                     makeFeed()
                 } else if (response.code() == 401){
                     Toast.makeText(applicationContext, "존재하지 않는 사용자입니다..", Toast.LENGTH_SHORT).show()
                 Log.e(ERROR_CODE_RETROFIT, "등록되지 않은 유저")
                 }
                 else {
                     Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                     Log.e(ERROR_CODE_RETROFIT, "response body is null")
                 }
             }
         })
     }

    fun clickJoin(){//sign in button onclick fun
        var intent = Intent(this, EnrollActivity::class.java)
        startActivity(intent)
    }

    fun getCategoryByUserId(user_id: Int): MutableList<MyCatg> {
        val ret = mutableListOf<MyCatg>()
        val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
        //get user's categories
        AuthService.getCategoryById(user_id).enqueue(
            object : Callback<ResponsePOJO> {
                override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                    Log.e(ERROR_CODE_RETROFIT, t.toString())
                    Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onResponse(
                    call: Call<ResponsePOJO>,
                    response: Response<ResponsePOJO>
                ) {
                    if (response.code() == 200) {
                        val res = response.body()
                        if (res != null) {

                            Log.v("retrofit", res.toString() + "categories")
                        } else {
                            Log.e(ERROR_CODE_RETROFIT, "response body is null")
                        }
                        makeFeed()
                    } else if (response.code() == 204) {
                        Log.e(ERROR_CODE_RETROFIT, response.message())
                    } else {
                        Log.e(ERROR_CODE_RETROFIT, response.message())
                    }
                }
            })
        return ret

    }

    fun getBooksByUserId(user_id: Int): MutableList<Book>{
        val ret = mutableListOf<Book>()
        val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
        //get user's categories
        AuthService.getBooksByUserId(user_id).enqueue(
            object : Callback<ResponsePOJO> {
                override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                    Log.e(ERROR_CODE_RETROFIT, t.toString())
                    Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()

                }
                override fun onResponse(call: Call<ResponsePOJO>, response: Response<ResponsePOJO>) {
                    if (response.code() == 200) {
                        val res = response.body()
                        Toast.makeText(applicationContext, "로그인", Toast.LENGTH_SHORT).show()
                        if (res != null) {
                            Log.v("retrofit", res.toString() + "login complete")

                        } else {
                            Log.e(ERROR_CODE_RETROFIT, "response body is null")
                        }
                        makeFeed()
                    } else if (response.code() == 401){
                        Toast.makeText(applicationContext, "존재하지 않는 사용자입니다..", Toast.LENGTH_SHORT).show()
                        Log.e(ERROR_CODE_RETROFIT, "등록되지 않은 유저")
                    }
                    else {
                        Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        Log.e(ERROR_CODE_RETROFIT, "response body is null")
                    }
                }
            })
        return ret
    }

    fun makeFeed(){//나에게 맞춰 피드를 만들어줌
        getCategoryByUserId(loggedInUserId)
//        var myBook:ArrayList<MyFeed>//나의 책 목록
//        var myFeedArr:ArrayList<MyFeed>
//
//        var want:ArrayList<MyFeed>//내가 찜한 책 목록
//
//        myBook = arrayListOf()
//        myFeedArr = arrayListOf()
//        want = arrayListOf()
//
//
//        var catg = user_map[user_id]!!.catg//현재 사용자의 카테고리 정보
//        var catg_arr = catg.split(" ")//공백으로 스플릿
//
//        for(i in 0 until feed.size){//나의 책 목록을 만든다
//            if(feed[i].no == user_id){
//                myBook.add(feed[i])//나의 책 목록
//            }
//        }
//
//        for(i in 0 until user.size){//내 피드 정보를 만든다
//            var feedNo = -1
//            var u_catg = user_map[user[i].no]!!.catg
//            var u_catg_arr = u_catg.split(" ")//다른 사람의 카테고리
//            var same_catg = false
//            brakPoint@for(j in 0 until u_catg_arr.size){
//                for(k in 0 until catg_arr.size){
//                    if(u_catg_arr[j] == catg_arr[k] && catg_arr[k]!=""){//같은 카테고리가 있으면 보여줌
//                        feedNo = user[i].no//나와 비슷한 카테고리를 가진 사람의 인덱스 번호
//                        same_catg = true
//                        break@brakPoint
//                    }
//                }
//            }
//            if(same_catg){
//                for(u in 0 until feed.size){
//                    if(feed[u].no == feedNo){
//                        myFeedArr.add(feed[u])//나와 비슷한 카테고리의 피드를 담음
//                    }
//                }
//            }
//        }
//
//        //글귀는 게시글 3개당 한개씩 끼워넣는다
//        var j = 0
//        for(i in 0 until myFeedArr.size){
//            if(i!=0&&i % 3 == 0 && j <= (letter.size-1)){
//                myFeedArr.add(i,letter[j++])
//            }
//        }
//
//        for(i in 0 until allwant.size){
//            if(allwant[i].no == user_id){//나인경우
//                want.add(allwant[i])
//            }
//        }
//
//        var intent = Intent(this,FeedActivity::class.java)
//        intent.putParcelableArrayListExtra("MYFEED",myFeedArr)//피드정보를 넘겨줌
//        intent.putParcelableArrayListExtra("FEED",feed)//모든피드
//        intent.putParcelableArrayListExtra("MYBOOK",myBook)//나의 책 정보를 넘겨줌
//        intent.putParcelableArrayListExtra("USER",user)//유저의 정보 넘겨줌(피드 정리할때)
//        intent.putParcelableArrayListExtra("LETTER",letter)//글귀 정보를 넘겨줌
//        intent.putExtra("MY",user_map[user_id])//나의 정보를 넘겨줌
//        intent.putParcelableArrayListExtra("ALLWANT",allwant)//모든 사람들의 찜 목록을 넘겨줌
//        intent.putParcelableArrayListExtra("WANT",want)//내가 찜한 목록을 넘겨줌
//        startActivity(intent)
    }

}
