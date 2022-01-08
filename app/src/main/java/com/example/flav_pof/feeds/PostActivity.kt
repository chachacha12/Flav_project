package com.example.flav_pof.feeds

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.example.flav_pof.FirebaseHelper
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import org.json.JSONObject


class PostActivity : BasicActivity() {

    private var contents: Contents? = null
    private var firebaseHelper: FirebaseHelper? = null
    private var readContentsVIew: ReadContentsVIew? = null
    private var contentsLayout: LinearLayout? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        contents = intent.getSerializableExtra("postInfo") as Contents
        Log.e("태그", "포스트액티비티로 받아온 intent.getSerializableExtra(\"postInfo\") as Contents: "+contents)

        //jsonobject들은 intent로 받아올때 따로 string으로 변환 후 가져옴.
        //그 후 여기서 다시 jsonobject로 만들어줄거임
        var user = intent.getStringExtra("user")
        var tag1 = intent.getStringExtra("tag1")
        var tag2 = intent.getStringExtra("tag2")
        var tag3 = intent.getStringExtra("tag3")
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
        firebaseHelper = FirebaseHelper(this,server)
        firebaseHelper!!.setOnPostListener(onPostListener)
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
    //피드에서 게시물 한번 눌러서 postactivity로 와서 점세계버튼 누른경우
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                firebaseHelper!!.storageDelete(contents!!)

                true
            }
            R.id.modify -> {
                //myStartActivity(WritePostActivity::class.java, postInfo!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var onPostListener: OnPostListener = object : OnPostListener {
        override fun onDelete(contents: Contents) {
            Log.e("로그 ", "onPostListener작동하여 삭제 성공")
            firebaseHelper!!.storageDelete(contents!!)
        }

        override fun onModify() {
            Log.e("로그 ", "수정 성공")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uiUpdate() {
        setToolbarTitle(contents!!.restname)
        readContentsVIew?.setContents(contents!!)

    }

    private fun myStartActivity(c: Class<*>, contents: Contents) {
        val intent = Intent(this, c)
        intent.putExtra("postInfo", contents)
        startActivityForResult(intent, 0)
    }

}






