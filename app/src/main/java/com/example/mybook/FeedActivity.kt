package com.example.mybook

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.mybook.model.MyFeed
import com.example.mybook.model.User
import com.example.mybook.retrofit.AuthServiceGenerator
import com.example.mybook.retrofit.BookmarkServer
import com.example.mybook.retrofit.ResponsePOJO
import com.example.mybook.retrofit.ServiceExecutor
import me.relex.circleindicator.CircleIndicator
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var adapterVeiwPager: FragmentPagerAdapter
    lateinit var my: User
    lateinit var user:ArrayList<User>//모든 피드
    lateinit var allFeed:ArrayList<MyFeed>//모든 피드
    lateinit var myFeedArr:ArrayList<MyFeed>
    lateinit var myBook:ArrayList<MyFeed>//나의 책 목록
    lateinit var letter:ArrayList<MyFeed>//글귀 목록
    lateinit var allwant:ArrayList<MyFeed>//모든 사람들의 찜 목록
    lateinit var want:ArrayList<MyFeed>
    lateinit var vpPager: ViewPager
    lateinit var fab_open: Animation
    lateinit var fab_close: Animation

    lateinit var fab: FloatingActionButton
    lateinit var fab1: FloatingActionButton
    lateinit var fab2: FloatingActionButton
    lateinit var token: String

    private var isFabOpen = false

    val ERROR_CODE_RETROFIT = "retrofit_error"
    val NORMAL_CODE_RETROFIT = "retrofit_normal"
    val SELECT_IMAGE = 9999
    val USE_INTERNET = 8888
    val REQ_CODE_SPEECH_INPUT = 5555
    var WRITE_CODE = 7654
    val LETTER_CODE = 3678

    lateinit var myFeedMake:mainfeed
    lateinit var myProfile:mypage
    lateinit var searchMake:searchpage
    lateinit var calendar:CalendarFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        fab_open= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open)
        fab_close= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close)

        fab=findViewById(R.id.fab)
        fab1=findViewById(R.id.fab1)
        fab2 = findViewById(R.id.fab2)

        fab.setOnClickListener(this)
        fab1.setOnClickListener(this)
        fab2.setOnClickListener(this)


        vpPager= findViewById(R.id.vpPager)
        adapterVeiwPager = MyPagerAdapter(supportFragmentManager)
        vpPager.adapter=adapterVeiwPager

        //인디케이터 생성 부분
        var indicator: CircleIndicator = findViewById(R.id.indicator)

        indicator.setViewPager(vpPager)
        //사진 접근 허용,마이크 및 인터넷
        initPermission()
        init()
    }

    fun init(){
        my = intent.getParcelableExtra("MY")
        token = intent.getStringExtra("token")
        user = ArrayList()
        allFeed = ArrayList()
        myFeedArr = ArrayList()
        myBook = ArrayList()
        letter = ArrayList()
        allwant = ArrayList()
        want = ArrayList()

        ServiceExecutor.getFeedsByUserId(token, my.no,
            {response ->
                Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT)
                    .show()
            },
            {response ->
                if (response.code() == 200) {
                    val res = response.body()
                    Toast.makeText(applicationContext, "로그인", Toast.LENGTH_SHORT).show()
                    if (res != null) {
                        val jsonArray = JSONArray(res.payload)
                        for(i in 0..jsonArray.length() - 1){
                            myFeedArr.add(
                                MyFeed(
                                    no = jsonArray.getJSONObject(i).getInt("id"),
                                    author = jsonArray.getJSONObject(i).getString("author"),
                                    contents = jsonArray.getJSONObject(i).getString("contents"),
                                    imgsrc = jsonArray.getJSONObject(i).getString("imgUri"),
                                    date = jsonArray.getJSONObject(i).getString("createdAt"),
                                    user_id =  my.no,
                                    like = jsonArray.getJSONObject(i).getInt("like")
                                )
                            )
                        }
                        Log.v(NORMAL_CODE_RETROFIT, res.toString())

                    } else {
                        Log.e(ERROR_CODE_RETROFIT, "response body is null")
                    }
                } else if (response.code() == 401){
                    Log.e(ERROR_CODE_RETROFIT, "등록되지 않은 유저")
                }
                else {
                    Log.e(ERROR_CODE_RETROFIT, "response body is null")
                }
            }
        )
        getFeedsByUserId(user_id = my.no)
        getBooksByUserId(user_id = my.no)

//        user = getIntent().getParcelableArrayListExtra("USER")
//        allFeed = getIntent().getParcelableArrayListExtra("FEED")//모든 피드정보
//        myFeedArr = getIntent().getParcelableArrayListExtra("MYFEED")//나의 피드 정보
//        myBook = getIntent().getParcelableArrayListExtra("MYBOOK")//나의 책 정보
//        letter = getIntent().getParcelableArrayListExtra("LETTER")//글귀정보
//        allwant = getIntent().getParcelableArrayListExtra("ALLWANT")//모든 사람들의 찜 목록
//        want = getIntent().getParcelableArrayListExtra("WANT")//내가 찜한 목록

        myFeedMake = mainfeed()
        myProfile = mypage()
        searchMake=searchpage()
        calendar= CalendarFragment()


        Log.d("USERINFO",my.name)
    }

    fun initPermission(){//퍼미션 체크
        if(checkAppPermission(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))){//사진 체크
            Toast.makeText(this,"권한체크됨",Toast.LENGTH_SHORT).show()
        }else{
            askPermission(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),SELECT_IMAGE)
        }
        if(checkAppPermission(arrayOf(android.Manifest.permission.INTERNET))){//인터넷체크

        }else{
            askPermission(arrayOf(android.Manifest.permission.INTERNET),USE_INTERNET)
        }
        if(checkAppPermission(arrayOf(android.Manifest.permission.RECORD_AUDIO))){

        }else{
            askPermission(arrayOf(android.Manifest.permission.RECORD_AUDIO),REQ_CODE_SPEECH_INPUT)
        }
    }

    fun checkAppPermission(requestPermission:Array<String>):Boolean{
        val requestResult = BooleanArray(requestPermission.size)
        for(i in requestResult.indices){
            requestResult[i] = ContextCompat.checkSelfPermission(this,requestPermission[i]) == PackageManager.PERMISSION_GRANTED
            if(!requestResult[i]){
                return false
            }
        }
        return true
    }

    fun askPermission(requestPermission: Array<String>, REQ_PERMISSION:Int){
        ActivityCompat.requestPermissions(this,requestPermission,REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            SELECT_IMAGE->{
                if(checkAppPermission(permissions)){//허용이 된 경우
                    Toast.makeText(applicationContext,"권한을 허락함",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext,"권한을 허락안함",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            USE_INTERNET->{
                if(checkAppPermission(permissions)){//허용이 된 경우
                    Toast.makeText(applicationContext,"권한을 허락함",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext,"권한을 허락안함",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            REQ_CODE_SPEECH_INPUT->{
                if(checkAppPermission(permissions)){//허용이 된 경우
                    Toast.makeText(applicationContext,"권한을 허락함",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext,"권한을 허락안함",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }


    override fun onClick(p0: View?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        var id=p0!!.getId()
        when(id){
            R.id.fab-> {
                anim()
            }
            R.id.fab1->{
                anim()//글쓰기 인텐트로 넘어감
                var intent= Intent(getApplicationContext(),WriteActivity::class.java)
                intent.putExtra("USERNO", my)
                intent.putExtra("token", token)
                startActivityForResult(intent,WRITE_CODE)
            }
            R.id.fab2->{
                anim()
                //글귀저장 인텐트
                var intent = Intent(applicationContext,WritingActivity::class.java)
                intent.putExtra("USERNO",my.no)
                startActivityForResult(intent,LETTER_CODE)
            }
        }
    }
    fun anim() {
        if (isFabOpen) {
            fab1.startAnimation(fab_close)
            fab1.isClickable = false
            fab2.startAnimation(fab_close)
            fab2.isClickable = false
            isFabOpen = false
        } else {
            fab1.startAnimation(fab_open)
            fab1.isClickable = true
            fab2.startAnimation(fab_open)
            fab2.isClickable = true
            isFabOpen = true
        }
    }

    inner class MyPagerAdapter(fm: FragmentManager?): FragmentPagerAdapter(fm)
    {
        var num=4;

        override fun getItem(p0: Int): Fragment? {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            when(p0){
                //search
                0->{
                    Log.d("PAGER",vpPager.currentItem.toString())
                    searchMake=searchpage.makeSearch(allFeed,my,want,allwant)
                    return searchMake
                }
                //mainfeed
                1->{
                    Log.d("PAGER",vpPager.currentItem.toString())
                    myFeedMake = mainfeed.makeFeed(myFeedArr,my, token)
                    return myFeedMake
                }
                //mypage
                2->{
                    Log.d("PAGER",vpPager.currentItem.toString())
                     myProfile = mypage.makeProfile(my,want)//나의 프로필 정보 ,내가 찜한 책 정보를
                    return myProfile
                }
                3->{//캘린더
                    calendar = CalendarFragment.makeCalendar(myBook)//나의 책 정보 넘겨줌
                    return calendar
                }
            }
            return null
        }

        override fun getCount(): Int {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            return num
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {//글쓰기 종료되면
        super.onActivityResult(requestCode, resultCode, data)
        var myfeed: MyFeed
        if(requestCode==WRITE_CODE){//게시글추가
            if(resultCode==RESULT_OK){
                myfeed = data!!.getParcelableExtra("UPLOAD") as MyFeed
//                allFeed.add(myfeed)
                //나의 책목록 다시 생성
                getBooksByUserId(user_id = my.no)
                getFeedsByUserId(user_id = my.no)
                refresh()
            }
        }else if(requestCode == LETTER_CODE){///글귀추가 했을 때
            if(requestCode==RESULT_OK){
               myfeed = data!!.getParcelableExtra<MyFeed>("UPLOAD_LETTER") as MyFeed
                letter.add(myfeed)
                myFeedArr.clear()
                refresh()
            }
        }

    }

    fun refresh(){//나에게 맞춰 피드를 만들어줌
        //나의 카테고리정보

        myFeedArr
        myFeedMake.initlayout()
        myFeedMake.adapter.notifyDataSetChanged()
        //myFeedMake.adapter.notifyDataSetChanged
        //책정보 다시 넘겨주기
        searchMake.post = allFeed
        //캘린더에 내 책정보 넘겨주기
        calendar.myBook = myBook
    }

    fun getCategoryByUserId(user_id: Int) {

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
                            val jsonArray = JSONArray(res.payload)
                            for(i in 0..jsonArray.length() - 1){
                                val id = jsonArray.getJSONObject(i).getInt("id")
                                val name = jsonArray.getJSONObject(i).getString("name")

                            }
                            Log.v(NORMAL_CODE_RETROFIT, res.payload)
                        } else {
                            Log.e(ERROR_CODE_RETROFIT, "response body is null")
                        }
                    } else if (response.code() == 204) {
                        Log.e(ERROR_CODE_RETROFIT, response.message())
                    } else {
                        Log.e(ERROR_CODE_RETROFIT, response.message())
                    }
                }
            })


    }
    fun getFeedsByUserId(user_id: Int){

        val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
        //get user's categories
        AuthService.getFeedsByUserId(user_id).enqueue(
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
                            val jsonArray = JSONArray(res.payload)
                            for(i in 0..jsonArray.length() - 1){
                                myFeedArr.add(
                                    MyFeed(
                                        no = jsonArray.getJSONObject(i).getInt("id"),
                                        author = jsonArray.getJSONObject(i).getString("author"),
                                        contents = jsonArray.getJSONObject(i).getString("contents"),
                                        imgsrc = jsonArray.getJSONObject(i).getString("imgUri"),
                                        date = jsonArray.getJSONObject(i).getString("createdAt"),
                                        user_id =  my.no,
                                        like = jsonArray.getJSONObject(i).getInt("like")
                                    )
                                )
                            }
                            Log.v(NORMAL_CODE_RETROFIT, res.toString())

                        } else {
                            Log.e(ERROR_CODE_RETROFIT, "response body is null")
                        }
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
    fun getBooksByUserId(user_id: Int){
        val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
        //get user's categories
        AuthService.getFeedsByUserId(user_id).enqueue(
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
                            val jsonArray = JSONArray(res.payload)
                            for(i in 0..jsonArray.length() - 1){
                                myBook.add(
                                    MyFeed(
                                        no = jsonArray.getJSONObject(i).getInt("id"),
                                        author = jsonArray.getJSONObject(i).getString("author"),
                                        contents = jsonArray.getJSONObject(i).getString("contents"),
                                        imgsrc = jsonArray.getJSONObject(i).getString("imgUri"),
                                        date = jsonArray.getJSONObject(i).getString("createdAt"),
                                        user_id =  my.no
                                    )
                                )
                            }
                            Log.v(NORMAL_CODE_RETROFIT, res.toString())

                        } else {
                            Log.e(ERROR_CODE_RETROFIT, "response body is null")
                        }
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

}
