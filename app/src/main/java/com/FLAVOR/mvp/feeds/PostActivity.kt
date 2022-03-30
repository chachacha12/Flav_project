package com.FLAVOR.mvp.feeds  //이곳에서 댓글기능도 만들거임. Coments_RecyclerView와 ComentsAdapter는 여기에서 쓰임

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.activity.BasicActivity
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_post.*
import org.json.JSONObject


class PostActivity : BasicActivity() {

    private var contents: Contents? = null
    private var readContentsVIew: ReadContentsVIew? = null
    private var contentsLayout: LinearLayout? = null

    //댓글기능을 위한 변수들
    private var comentsAdapter: ComentsAdapter? = null
    private var comments:String ? = null  //댓글리스트를 전역으로둠. 어댑터로 쉽게 보내기 위해

    fun init(){  //댓글 리사이클러뷰 작업
        //recyclerView = recyclerView_user  //화면에 보일 리사이클러뷰객체
        coments_recyclerView.setHasFixedSize(true)
        coments_recyclerView.layoutManager = LinearLayoutManager(this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        init()

        //사용자가 선택한 컨텐츠 날라온 값을 받음
        contents = intent.getSerializableExtra("postInfo") as Contents
        Log.e("태그", "포스트액티비티로 받아온 intent.getSerializableExtra(\"postInfo\") as Contents: "+contents)

        //Contents클래스객체의 jsonobject들은 intent로 받아올때 따로 string으로 변환 후 가져와야만 되었음
        //그 후 여기서 다시 jsonobject로 만들어줄거임
        var user = intent.getStringExtra("user")
        var tag1 = intent.getStringExtra("tag1")
        var tag2 = intent.getStringExtra("tag2")
        var tag3 = intent.getStringExtra("tag3")
         comments = intent.getStringExtra("comments")  //해당 게시물의 댓글들 목록 스트링으로 가져옴
        Log.e("태그", "포스트액티비티로 받아온 유저, 태그값들: "+user+ tag1+tag2+tag3)

        var user_json = JSONObject(user)  //받아온 string값을 jsonobject로 변경
        contents!!.User =user_json      //contents의 User요소를 덮어씌움. 가져온걸로.
        var tag1_json = JSONObject(tag1)
        contents!!.Tag_FirstAdj =tag1_json
        var tag2_json = JSONObject(tag2)
        contents!!.Tag_SecondAdj =tag2_json
        var tag3_json = JSONObject(tag3)
        contents!!.Tag_Location =tag3_json

        contentsLayout = findViewById(R.id.contentsLayout)
        readContentsVIew = findViewById(R.id.readContentsView)

        //게시물 삭제,s3삭제로직 수행하는 객체 초기화
        uiUpdate()
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
        readContentsVIew?.setContents(contents!!)

        //리사이클러뷰를 여기서 제대로 만들어줌.
        comentsAdapter = ComentsAdapter(
            this, comments!!, server
        )
        coments_recyclerView.adapter = comentsAdapter    //리사이클러뷰의 어댑터에 내가 만든 어댑터 붙힘. 사용자가 게시글 지우거나 수정 등 해서 데이터 바뀌면 어댑터를 다른걸로 또 바꿔줘야함 ->notifyDataSetChanged()이용

    }




}






