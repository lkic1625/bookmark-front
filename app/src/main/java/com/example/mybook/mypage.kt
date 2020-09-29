package com.example.mybook


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mybook.model.Book
import com.example.mybook.model.MyFeed
import com.example.mybook.model.User
import com.example.mybook.retrofit.ServiceExecutor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_mypage.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class mypage : Fragment() {


    lateinit var my: User
    lateinit var myBook:ArrayList<MyFeed>//나의 책 목록
    lateinit var adapter:sbookAdapter//어댑터
    val SELECT_IMAGE = 9999
    var token = "";
    lateinit var profileImageBitmap:Bitmap
    lateinit var db:FirebaseFirestore
    lateinit var myF:mypage
    lateinit var myBookList:ArrayList<Book>
    var check = false

    companion object{
        val myFrag = mypage()
        fun makeProfile(user: User, book:ArrayList<MyFeed>, token :String ):mypage{
            myFrag.myF = mypage()
            myFrag.my = user
            myFrag.myBook = book
            myFrag.token = token;
            return myFrag
        }

        fun send(item:ArrayList<MyFeed>){
            myFrag.myBook = item
            Log.d("번들",myFrag.myBook.size.toString())
            myFrag.myBookList = arrayListOf()
            myFrag.myBookList.clear()
            for(i in 0 until item.size){
                myFrag.myBookList.add(Book(myFrag.myBook[i].bname,myFrag.myBook[i].author,0,0,myFrag.myBook[i].publisher,"null","null","null","null",myFrag.myBook[i].imageLink))
            }
            Log.d("호출0",myFrag.myBook.size.toString())
            Log.d("호출1",myFrag.myBookList.size.toString())
            myFrag.check = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        db = FirebaseFirestore.getInstance()//프로필사진 바꿨을 때 업데이트 하기 위해
        Log.d("호출","호출")
        makeMyInfo()
        makeMyList()
    }

    fun makeMyInfo(){
        my_user_name.text = myFrag.my.name//이름 설정
        Glide.with(this).load(my.uri).into(my_photo)
        //my_photo.setImageURI(myFrag.my.uri)
        user_book.text = myFrag.my.name+"님이 찜한 책"

        my_photo.setOnClickListener {
            //프로필 사진 바꾸기
            var intent = Intent(Intent.ACTION_PICK)
            intent.type=android.provider.MediaStore.Images.Media.CONTENT_TYPE
            intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent,SELECT_IMAGE)
        }
    }

    fun makeMyList(){//나의 책정보 리스트 만들기
        var b: Book
        b = Book("","",0,0,"","","","","","")
        if(check){
            b = myFrag.myBookList[myFrag.myBookList.size-1]
        }


        myBookList = arrayListOf()
        myBookList.clear()
        if(check){
             myBookList.add(b)
        }

        Log.d("책SIZE",myFrag.myBook.size.toString())
        for(i in 0 until myBook.size){
            myBookList.add(Book(myBook[i].bname,myBook[i].author,0,0,myBook[i].publisher,"null","null","null","null",myBook[i].imageLink))
        }
        Log.d("책SIZE",myBookList.size.toString())
        adapter=sbookAdapter(myBookList)//나의 책 정보가 담긴 책 리스트
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        readList.layoutManager = layoutManager
        readList.adapter = adapter
        adapter.notifyDataSetChanged()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//프로필사진 바꾸는 인텐트
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SELECT_IMAGE){
            if(resultCode == Activity.RESULT_OK){
                Log.d("MYURI",my.uri.toString())
                var uri = data!!.data as Uri
                my.uri = uri
                Log.d("MYURI",uri.toString())
                my_photo.setImageURI(my.uri)
                try {
                    profileImageBitmap = BitmapFactory.decodeStream(context!!.contentResolver.openInputStream(uri))
                } catch(e:FileNotFoundException){
                    e.printStackTrace()
                }
                updateProfile()
            }
        }
    }
    fun findProfile(){

    }

    fun updateProfile(){

        try {
            val dir = context!!.filesDir;
            val file = File(dir, dir.name + ".png");

            val bos = ByteArrayOutputStream();
            profileImageBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapData = bos.toByteArray()

            val fos = FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush()
            fos.close()

            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("img", file.name, reqBody)
            val user_id = (my.no.toString()).toRequestBody("text/plain".toMediaTypeOrNull())

            ServiceExecutor.uploadProfileImage(token, body, my.no,
                {
                    var res = it.body()
                    if(res != null) {
                        Toast.makeText(
                            activity!!.applicationContext,
                            "서버 오류로 잠시 후 다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(ServiceExecutor.ERROR_CODE_RETROFIT, res.message)
                    }
                },
                {
                    var res = it.body()
                    if(res != null){
                        if(it.code() == 201) {
                            my.uri = Uri.parse(res.payload)
                            Glide.with(this).load(res.payload).into(my_photo)
                        }
                        Log.v(ServiceExecutor.NORMAL_CODE_RETROFIT, res.message);
                    }
                })



        } catch (e:Exception){
            e.printStackTrace()
        }

    }

}
