package com.example.mybook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.GridLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mybook.model.Book
import com.example.mybook.model.MyFeed
import com.example.mybook.model.User
import com.example.mybook.retrofit.AuthServiceGenerator
import com.example.mybook.retrofit.BookmarkServer
import com.example.mybook.retrofit.ResponsePOJO
import com.example.mybook.retrofit.ServiceExecutor
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_write.*
import kotlinx.android.synthetic.main.fragment_mypage.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WriteActivity : AppCompatActivity() {

    val SELECT_IMAGE = 9999
    var my_no=-1
    lateinit var loggedInUser: User
    lateinit var uri_ocr:Uri
    lateinit var token:String
    var profileImageBitmap: Bitmap? = null
    var uri_Pic:Uri=Uri.parse("")
    lateinit var enrol_book: Book
    var isOCR=false

    var searchlist:ArrayList<Book> = ArrayList()
    lateinit var fadapter:sbookAdapter

    var myYear=""
    var myMonth=""
    var myDay=""

    val ERROR_CODE_RETROFIT = "retrofit_error"
    val NORMAL_CODE_RETROFIT = "retrofit_normal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        init()
    }

    fun textSearch(view:View?){

        writesearch.visibility = VISIBLE
        searchlist.clear()
        fadapter.notifyDataSetChanged()
        var searcht=btitle.text.toString()
        var searcha=bauthor.text.toString()
        var searchp=bpublish.text.toString()

        var bl=""
        var bt=""
        var ba=""
        var bp=""

        val handler = Handler() {
            fadapter= sbookAdapter(searchlist,clickFun)
            initlay()
            true
        }

        val run = Runnable {
            lateinit var apiReturnList:MutableList<Book>
            if(searcha==""&&searchp==""){//저자, 출판사 안 적으면 제목으로 검색
                apiReturnList=BookAPI.calladvAPI(d_titl = searcht)
            }else if(searchp==""){//출판사를 안 적은 경우 제목과 작가로 검색
                apiReturnList=BookAPI.calladvAPI(d_titl = searcht,d_auth = searcha)
            }else if(searcha==""){//작가만 안 적은 경우 제목과 출판사로 검색
                apiReturnList=BookAPI.calladvAPI(d_titl = searcht,d_publ = searchp)
            }else{//제목, 작가, 출판사 다 입력한 경우
            apiReturnList=BookAPI.calladvAPI(d_titl = searcht,d_auth = searcha,d_publ = searchp)
            }

            for (book in apiReturnList) {
                bl=book.imageLink
                Log.d("이미지링크",bl)

                bt=book.title
                bt=bt.replace("<b>","")
                bt=bt.replace("</b>","")

                ba=book.author
                ba=ba.replace("<b>","")
                ba=ba.replace("</b>","")

                bp=book.publisher
                bp=bp.replace("<b>","")
                bp=bp.replace("</b>","")

                searchlist.add(Book(bt,ba,book.price,book.discount,bp,book.pubdate,book.isbn,book.link,book.description,book.imageLink))
            }
            val msg = handler.obtainMessage()
            handler.sendMessage(msg)
        }
        val th = Thread(run)
        th.start()
    }

    val clickFun:(Book) -> Unit = {
        enrol_book = it//내가 등록한 책의 정보
        btitle.setText(it.title)
        bauthor.setText(it.author)
        bpublish.setText(it.publisher)
        Log.d("clickFuntest",it.title)
        writesearch.visibility = GONE
    }

    fun init(){

        fadapter= sbookAdapter(searchlist,clickFun)
        loggedInUser = intent.getParcelableExtra("USERNO")
        my_no = loggedInUser.no
        token = intent.getStringExtra("token")
        initlay()
        showDatePicker()
    }

    fun showDatePicker() {

        var now = System.currentTimeMillis()//현재 시간
        var date:Date = Date(now)//현재 날짜

        var yearFormat: SimpleDateFormat = SimpleDateFormat("yyyy",Locale.getDefault())//현재 지역의 년도
        var monthFormat: SimpleDateFormat = SimpleDateFormat("MM",Locale.getDefault())//현재 지역의 달
        var dayFormat: SimpleDateFormat = SimpleDateFormat("dd",Locale.getDefault())//현재 몇일

        myYear = yearFormat.format(date)
        myMonth = monthFormat.format(date)
        myDay = dayFormat.format(date)

      datePick.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
          myYear = year.toString()
          if(monthOfYear<10){
              myMonth="0"+(monthOfYear+1).toString()
          }else{
              myMonth = (monthOfYear+1).toString()
          }
          if(dayOfMonth<10){
              myDay = "0"+dayOfMonth.toString()
          }else{
              myDay = dayOfMonth.toString()
          }
      }
    }

    fun initlay(){
        val layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        writesearch.layoutManager=layoutManager
        writesearch.adapter=fadapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==SELECT_IMAGE){
            if(resultCode == RESULT_OK){
                if(isOCR){//OCR
                    uri_ocr = Uri.parse(data!!.data.toString())
                    writesearch.visibility = View.GONE
                    pgb1.visibility = View.VISIBLE

                    useOCR()
                    isOCR = false
                }else{
                    uri_Pic = Uri.parse(data!!.data.toString())
                    myUploadPic.setImageURI(uri_Pic)
                    try {
                        profileImageBitmap = BitmapFactory.decodeStream(applicationContext.contentResolver.openInputStream(uri_Pic))
                    } catch(e: FileNotFoundException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    var clickFunction:(String)->Unit={
        btitle.text.append(it)
    }

    fun useOCR(){
        var image = FirebaseVisionImage.fromFilePath(applicationContext,uri_ocr!!)

        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(Arrays.asList("en", "ko"))
            .build()

        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)
        lateinit var result: FirebaseVisionText
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                result = firebaseVisionText
                val text = result.text
                val list = text.split(" ","\n").toMutableList()
                val layoutManager = GridLayoutManager(applicationContext,3, GridLayout.VERTICAL,false)
                writesearch.visibility = VISIBLE
                Log.d("개어렵네",text)
                writesearch.layoutManager = layoutManager
                writesearch.adapter = OCRTextAdapter(list,clickFunction)
                writesearch.visibility = View.VISIBLE
                pgb1.visibility = View.GONE
            }
            .addOnFailureListener {
                Log.d("개어렵네",it.message)
            }
    }



    fun submitPost(view: View?){//게시글 등록
        feedUpload()
       //        var db=FirebaseFirestore.getInstance()
//        var input:Map<String,String>
//        input = HashMap()
//        input.put("content",input_content.text.toString())
//        input.put("u_no",my_no.toString())
//        input.put("uri", uri_Pic.toString())
//        input.put("isbn",enrol_book.isbn)
//        input.put("author",enrol_book.author)
//        input.put("bname",enrol_book.title)
//        input.put("publisher",enrol_book.publisher)
//        input.put("imageLink",enrol_book.imageLink)
//        input.put("like_no", "")
//        input.put("date",myYear+myMonth+myDay)
//        db.collection("post").add(input)
//            .addOnSuccessListener {
//                Log.d("SUCCESS","게시글 등록 성공")
//            }
//            .addOnFailureListener {
//                Log.d("FAULURE","게시글 등록 실패")
//            }



    }

    fun OCRBtn(view:View?){
        isOCR = true
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)
    }

    fun feedUpload(){
        try {
            val dir = applicationContext.filesDir;
            val file = File(dir, dir.name + ".png");

            val bos = ByteArrayOutputStream();
            if(profileImageBitmap != null)
                profileImageBitmap!!.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapData = bos.toByteArray()

            val fos = FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush()
            fos.close()

            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("img", file.name, reqBody)

            val AuthService = AuthServiceGenerator.createService(BookmarkServer::class.java, token)
            //get user's categories
            AuthService.uploadFeed(
                image=body,
                user_id = my_no,
                author = loggedInUser.name,
                contents = input_content.text.toString(),
                imgUri = uri_Pic.toString(),
                book_author = enrol_book.author,
                book_name =  enrol_book.title,
                book_isbn = enrol_book.isbn,
                book_publisher = enrol_book.publisher,
                book_imageLink = enrol_book.imageLink
            ).enqueue(
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
                                Log.v(NORMAL_CODE_RETROFIT, res.toString())
                                var tmpFeed = MyFeed(my_no,uri_Pic.toString(),enrol_book.title,enrol_book.author,enrol_book.publisher,input_content.text.toString(),0,enrol_book.imageLink,enrol_book.isbn,myYear+myMonth+myDay)
                                var intent = Intent(applicationContext ,FeedActivity::class.java)
                                //put해주기 나중에
                                intent.putExtra("UPLOAD",tmpFeed)//현재 업로드한 피드 추가해주기
                                setResult(RESULT_OK,intent)
                                finish()
                            } else {
                                Log.e(ERROR_CODE_RETROFIT, "response body is null")
                            }
                        }
                        else {
                            Toast.makeText(applicationContext, "서버 오류로 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            Log.e(ERROR_CODE_RETROFIT, response.message())
                        }
                    }
                })


        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun PicBtn(view:View?){//사진 업로드
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, SELECT_IMAGE)
    }

}