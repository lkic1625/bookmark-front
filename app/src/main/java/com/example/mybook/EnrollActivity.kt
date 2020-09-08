package com.example.mybook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mybook.retrofit.ResponsePOJO
import com.example.mybook.retrofit.RetrofitUtility
import com.example.mybook.security.Hashing
import kotlinx.android.synthetic.main.activity_enroll.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStreamReader

class EnrollActivity : AppCompatActivity() {//회원가입 액티비티

    val REST_API = RetrofitUtility.getServer()
    val ERROR_CODE_RETROFIT = "retrofit"

    lateinit var newUser:ArrayList<User>//전 액티비티에서 받은 유저 정보
    lateinit var category:ArrayList<MyCatg>//전체 카테고리 목록
    lateinit var selected_catg:ArrayList<MyCatg>//내가 선택한 카테고리
    lateinit var my:User
    lateinit var adapter: CatgAdapter//카테고리 어댑터
    var id:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enroll)
        init()
    }

    fun init(){
        btn_signUp.setOnClickListener {
            signUp()
        }
        val categories_name = resources.getStringArray(R.array.categories);

        category = arrayListOf()
        selected_catg = arrayListOf()

        for (i in 10..34){
            val code = (i * 10)
            val myCatg = MyCatg(code, categories_name[i - 10])
            category.add(myCatg)
        }

        adapter = CatgAdapter(category)
        catg_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        catg_list.adapter = adapter

        adapter.itemClickListener = object : CatgAdapter.OnItemClickListener{
            override fun OnItemClick(holder: CatgAdapter.ViewHolder, view: View, data: MyCatg, position: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Toast.makeText(view.context,data.name,Toast.LENGTH_SHORT).show()
                selected_catg.add(data)
            }
        }
    }

    fun signUp(){
        var email = input_email.text.toString()
        var name = input_name.text.toString()
        var pw = Hashing.calculateHash(input_pass.text.toString())

        REST_API.signUp(email, pw, name, selected_catg).enqueue(object :
            Callback<ResponsePOJO> {
            override fun onFailure(call: Call<ResponsePOJO>, t: Throwable) {
                Log.e(ERROR_CODE_RETROFIT, t.toString())
                Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT);
            }
            override fun onResponse(call: Call<ResponsePOJO>, response: Response<ResponsePOJO>) {
                if(response.code() == 200){
                    val res = response.body()
                    Toast.makeText(applicationContext, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    if(res != null){
                        Log.v("retrofit", res.toString())
                    } else{
                        Log.e(ERROR_CODE_RETROFIT, "response body is null")
                    }
                    finish()
                }
                else if(response.code() == 202){
                    Toast.makeText(applicationContext, "이미 존재하는 사용자입니다.", Toast.LENGTH_SHORT).show()
                    Log.e(ERROR_CODE_RETROFIT, "등록된 유저")
                } else{
                    Log.e(ERROR_CODE_RETROFIT, "server-side error")
                }
            }
        })
    }

}
