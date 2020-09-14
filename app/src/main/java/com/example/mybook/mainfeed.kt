package com.example.mybook


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mybook.model.MyFeed
import com.example.mybook.model.User
import com.example.mybook.retrofit.ServiceExecutor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_mainfeed.*

class mainfeed : Fragment() {

    var data:ArrayList<MyFeed> = ArrayList()
    lateinit var adapter:MyFeedAdapter
    lateinit var my: User
    lateinit var db: FirebaseFirestore
    lateinit var token:String
    lateinit var loggedInUser: User

    companion object{

        var myFrag = mainfeed()

        fun makeFeed(feed:ArrayList<MyFeed>, u: User, token:String):mainfeed{
            myFrag.data = feed
            myFrag.my = u
            myFrag.token = token;
            return myFrag
        }


    }

    private fun likeFeed(position:Int): Unit {//종하요
        myFrag.data[position].like += 1
        myFrag.initlayout()

        TODO("좋아요 서버 기능 추가 Service Executor 작성.")

//            for (i in 0 until likeArr.size) {
//                if (likeArr[i] == myFrag.my.no.toString()) {//만약 이미 좋아요 했으면
//                    Toast.makeText(myFrag.activity, "이미 좋아요한 게시글입니다!",Toast.LENGTH_SHORT).show()
//                    check = false
//                    return false
//                }
//            }
//            if (check){//새로운 like정보 등록
//                 myFrag.data[position].like += myFrag.my.no.toString() + " "
//                Toast.makeText(myFrag.activity, "♥",Toast.LENGTH_SHORT).show()
//            Log.d("LIKE_PEOPLE", myFrag.data[position].like)
//            var document_name = "" as String
//            myFrag.db = FirebaseFirestore.getInstance()//좋아요 추가 바꿨을 때 업데이트 하기 위해
//            myFrag.db.collection("post").get()
//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        for (document in it.result!!) {
//                            if (myFrag.data[position].isbn == document.data["isbn"] && myFrag.data[position].no.toString() == document.data["u_no"] && myFrag.data[position].contents == document.data["content"]) {//책의 정보가 같을 때 등록한 사람도 같을 때
//                                document_name = document.id
//                                Log.d("DOCUMENT_NAME",document_name)
//                                break
//                            }
//                        }
//                        var input: MutableMap<String, String> = mutableMapOf()
//                        input.put("like_no", myFrag.data[position].like)
//                        input.put("author",myFrag.data[position].author)
//                        input.put("bname",myFrag.data[position].bname)
//                        input.put("content",myFrag.data[position].contents)
//                        input.put("date",myFrag.data[position].date)
//                        input.put("imageLink",myFrag.data[position].imageLink)
//                        input.put("isbn",myFrag.data[position].isbn)
//                        input.put("publisher", myFrag.data[position].publisher)
//                        input.put("u_no", myFrag.data[position].no.toString())
//                        input.put("uri",myFrag.data[position].imgsrc)
//                        myFrag.db.collection("post").document(document_name).set(input as MutableMap<String, Any>)
//                            .addOnSuccessListener {
//                                Log.d("UPDATE", "성공")
//                            }
//                            .addOnFailureListener {
//                                Log.e("UPDATE", "실패")
//                            }
//                    }
//                }
//                //text정보 업데이트
//
//                return true
//       }
//            return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mainfeed, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("프래그먼트","메인피드")
        init()
    }
    fun init(){
        adapter= MyFeedAdapter(data) { position: Int ->
            ServiceExecutor.like(token,
                my.no,
                myFrag.data[position].no,
                {
                    var res = it.body()
                    if(res != null){
                        if(it.code() == 200) myFrag.data[position].like += 1
                        Log.v(ServiceExecutor.NORMAL_CODE_RETROFIT, res.message);
                    }
                },
                {
                    var res = it.body()
                    if(res != null){
                        Toast.makeText(activity!!.applicationContext, "서버 에러 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                        Log.e(ServiceExecutor.ERROR_CODE_RETROFIT, res.message)
                    }
                }
            )
            myFrag.initlayout()
        }
        initlayout()

    }

    fun initlayout(){
        val layoutManager= LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        listView.layoutManager=layoutManager
        listView.adapter=adapter
        adapter.notifyDataSetChanged()
    }



}