package com.FLAVOR.mvp.feeds  //이곳에서 댓글기능도 만들거임. Coments_RecyclerView와 ComentsAdapter는 여기에서 쓰임

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.activity.BasicActivity
import com.FLAVOR.mvp.classes.CommentUpload_response
import com.FLAVOR.mvp.classes.Msg
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.feeds.ComentsAdapter
import com.FLAVOR.mvp.feeds.Contents
import com.FLAVOR.mvp.feeds.ReadContentsVIew
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_post.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class PostActivity : BasicActivity() {

    private var contents: Contents? = null       //내가 선택해서 가져온 게시물 객체.  댓글리사이클러뷰에도 이거 보내줘서 댓글리스트 만들거임.
    private var readContentsVIew: ReadContentsVIew? = null
    private var contentsLayout: LinearLayout? = null

    //댓글기능을 위한 변수들
    private var comentsAdapter: ComentsAdapter? = null
    private lateinit var commentsList: ArrayList<JSONObject>  //댓글 추가하거나 삭제할때 바로 업데이트 해주기위해 클라이언트단에서 저장해서 먼저 보여주는 댓글리스트

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        init()  //사용자가 선택한 게시물의 데이터들을 받아서 ui로 보여줌
        coments_ready() //댓글기능준비
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(){
        //사용자가 선택한 컨텐츠 날라온 값을 받음
        contents = intent.getSerializableExtra("postInfo") as Contents
        Log.e("태그", "포스트액티비티로 받아온 intent.getSerializableExtra(\"postInfo\") as Contents: "+contents)

        //Contents클래스객체의 jsonobject들은 intent로 받아올때 따로 string으로 변환 후 가져와야만 되었음
        //그 후 여기서 다시 jsonobject로 만들어줄거임
        var user = intent.getStringExtra("user")
        var tag1 = intent.getStringExtra("tag1")
        var tag2 = intent.getStringExtra("tag2")
        var tag3 = intent.getStringExtra("tag3")
        var comments = intent.getStringExtra("comments") //해당 게시물의 댓글들 목록 스트링으로 가져옴
        Log.e("태그", "포스트액티비티로 받아온 유저, 태그값들: "+user+ tag1+tag2+tag3)

        var user_json = JSONObject(user)  //받아온 string값을 jsonobject로 변경
        contents!!.User =user_json      //contents의 User요소를 덮어씌움. 가져온걸로.
        var tag1_json = JSONObject(tag1)
        contents!!.Tag_FirstAdj =tag1_json
        var tag2_json = JSONObject(tag2)
        contents!!.Tag_SecondAdj =tag2_json
        var tag3_json = JSONObject(tag3)
        contents!!.Tag_Location =tag3_json

        var comments_json = JSONArray(comments)
        contents!!.Comments =comments_json        //게시물에 댓글jsonarray저장

        commentsList = ArrayList()  //어댑터에 보내주는 댓글리스트 초기화
        commentsList.clear()
        //댓글 JsonArray를 하나하나 돌며 Jsonobject들을 댓글리스트에 저장해줌. 어댑터로 보내서 리사이클러뷰 만들거임
        var i=0
        repeat(comments_json.length()){
            val Object = comments_json.getJSONObject(i)  //카카오id와 댓글내용 들어있는 Jsonobject
            commentsList?.add(Object)
            i++
        }
        Log.e("태그","commentsList: "+commentsList)

        contentsLayout = findViewById(R.id.contentsLayout)
        readContentsVIew = findViewById(R.id.readContentsView)

        //게시물 삭제,s3삭제로직 수행하는 객체 초기화
        uiUpdate()
    } //init

    //댓글기능 준비작업
    fun coments_ready(){
        //댓글 리사이클러뷰 작업
        //recyclerView = recyclerView_user  //화면에 보일 리사이클러뷰객체
        coments_recyclerView.setHasFixedSize(true)
        coments_recyclerView.layoutManager = LinearLayoutManager(this)

        //댓글등록버튼클릭
        save_comment_button.setOnClickListener {
            Log.e("태그", "댓글 업로드 버튼 클릭함")
            comments_Upload(Usersingleton.kakao_id.toString(),wirtecomments_editText.text.toString())
        }
    } //coments_ready


    //댓글업로드로직
    fun comments_Upload(kakao_id: String, content: String){
        server.comment_upload_Request(
            contents?.contents_id.toString(), kakao_id,
            content
        ).enqueue(object : Callback<CommentUpload_response> {
            override fun onFailure(
                call: Call<CommentUpload_response>,
                t: Throwable
            ) {
                Log.e("태그", "댓글 업로드 통신 아예실패  ,t.message: " + t.message)
                Toast.makeText(this@PostActivity, "댓글 업로드 실패", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<CommentUpload_response>,
                response: Response<CommentUpload_response>
            ) {
                if (response.isSuccessful) {
                    Log.e("태그", "댓글업로드 통신성공 ,msg: " + response.body()?.msg)
                    Log.e("태그", "댓글업로드 통신 성공  ,msg: " + response.body()?.toString())

                    //댓글 추가하자마자 화면에 보여주기위한 작업.
                    var new_comment = JSONObject()
                    new_comment.put("kakao_id",Usersingleton.kakao_id)
                    new_comment.put("content", wirtecomments_editText.text.toString())
                    Log.e("태그", "새로추가한 댓글 ,kakao_id와 content : " + kakao_id+", "+content)
                    commentsList.add(new_comment)

                    //리사이클러뷰 데이터 업데이트된걸 알려주고 어댑터 다시 붙여줌
                    comentsAdapter?.notifyDataSetChanged()
                    coments_recyclerView.adapter = comentsAdapter

                    wirtecomments_editText.setText(null)  //적어둔 값을 지워줌

                    Toast.makeText(this@PostActivity, "댓글 업로드 완료", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(
                        "태그",
                        "댓글업로드 서버접근했지만 실패: response.errorBody()?.string()" + response.errorBody().toString()
                    )
                    Log.e(
                        "태그",
                        "  response.message()" + response.message()
                    )
                    Toast.makeText(this@PostActivity, "댓글 업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode == RESULT_OK) {
                contents = data!!.getSerializableExtra("postinfo") as Contents
                Log.e("태그", "수정 등 했다가 다시 돌아온 contents: "+contents)
                contentsLayout!!.removeAllViews()
                uiUpdate()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post, menu)
        return super.onCreateOptionsMenu(menu)
    }


    //여기서 실제로 레이아웃의 뷰들에 컨텐츠 값들 채워넣어줌
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uiUpdate() {
        setToolbarTitle(contents!!.restname)
        readContentsVIew?.setContents(contents!!,false)

        //리사이클러뷰를 여기서 제대로 만들어줌.
        comentsAdapter = ComentsAdapter(
            this, commentsList, server
        )
        coments_recyclerView.adapter = comentsAdapter    //리사이클러뷰의 어댑터에 내가 만든 어댑터 붙힘. 사용자가 게시글 지우거나 수정 등 해서 데이터 바뀌면 어댑터를 다른걸로 또 바꿔줘야함 ->notifyDataSetChanged()이용

    }




}






