package com.example.mybook

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mybook.model.Book
import com.example.mybook.model.MyFeed
import com.example.mybook.model.User
import com.example.mybook.retrofit.ServiceExecutor
import com.google.firebase.firestore.FirebaseFirestore
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_look_book.*

class LookBookActivity : AppCompatActivity() {

    val client_token_key = "ae4d3f78-642b-4fd7-8501-6e101e3b39c0";
    lateinit var searchedBook: Book
    lateinit var allPost:ArrayList<MyFeed>
    lateinit var my: User
    lateinit var want:ArrayList<MyFeed>
    lateinit var allwant:ArrayList<MyFeed>
    lateinit var token:String
    var postlist:ArrayList<MyFeed> = ArrayList()
    lateinit var padapter:MyFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_look_book)
        getdata()
    }
    //우선 테스트로 출력해놓음
    //가져온 책 정보로 post DB에서 동일한 isbn인 책 포스트만 가져오기
    fun getdata(){
        searchedBook= intent.extras.getSerializable("sbook") as Book//현재 책 정보
        allPost = getIntent().getParcelableArrayListExtra("ALLPOST")//모든 피드의 정보
        my = intent.getParcelableExtra("MY")//나의 정보
        want = getIntent().getParcelableArrayListExtra("WANT")//나의 찜 정보
        allwant = getIntent().getParcelableArrayListExtra("ALLWANT")
        token = intent.getStringExtra("token")
        setlayout()
    }
    fun setlayout(){
        Glide.with(applicationContext).load(searchedBook.imageLink).into(lb_img)

        lb_title.text=searchedBook.title
        lb_author.text=searchedBook.author
        lb_publish.text=searchedBook.publisher

        setsearchre.text="\""+searchedBook.title+"\" 일치하는 POST 검색 결과"
        val layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        lb_rview.layoutManager=layoutManager
        getpost()
    }
    fun getpost(){
        ServiceExecutor.getFeedByBookId(token, book_id = 1)
    }

    fun addwant(view: View?){

        //내가 찜한 목록에 있는지 비교
        var check = true

        if(check){//같은 책이 없음
            Toast.makeText(this,"♥",Toast.LENGTH_SHORT).show()
            ServiceExecutor.wish(token, my.no, searchedBook,
                {
                    var res = it.body()
                    if(res != null) {
                        Toast.makeText(
                            applicationContext,
                            "네트워크 오류",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(ServiceExecutor.ERROR_CODE_RETROFIT, res.message)
                    }
                },
                {
                    var res = it.body()
                    if(res != null) {
                        if(it.code() == 201 ) {
                            want.add(MyFeed(my.no,searchedBook.link,searchedBook.title,searchedBook.author,searchedBook.publisher, "",0,searchedBook.imageLink,searchedBook.isbn,""))
                            mypage.send(want)
                            Log.e(ServiceExecutor.NORMAL_CODE_RETROFIT, res.message)

                        }
                    }
                });
            var added:Map<String,String>
            added = hashMapOf()
            added.put("author",searchedBook.author)
            added.put("bname",searchedBook.title)
            added.put("imageLink",searchedBook.imageLink)
            added.put("isbn",searchedBook.isbn)
            added.put("publisher",searchedBook.publisher)
            added.put("u_no",my.no.toString())

            //  var mypage=mypage()
           // var bundle:Bundle=Bundle()
           // bundle.putParcelableArrayList("WANT_DATA",want)

          //  Log.d("번들비지라마",bundle.toString())
           // mypage.setArguments(bundle)

        }
    }
}