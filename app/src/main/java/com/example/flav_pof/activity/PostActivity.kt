package com.example.flav_pof.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.example.flav_pof.FirebaseHelper
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.listener.OnPostListener
import com.example.flav_pof.view.ReadContentsVIew


class PostActivity : BasicActivity() {

    private var postInfo: PostInfo? = null
    private var firebaseHelper: FirebaseHelper? = null
    private var readContentsVIew: ReadContentsVIew? = null
    private var contentsLayout: LinearLayout? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        postInfo = intent.getSerializableExtra("postInfo") as PostInfo

        contentsLayout = findViewById(R.id.contentsLayout)
        readContentsVIew = findViewById(R.id.readContentsView)

        firebaseHelper = FirebaseHelper(this)
        firebaseHelper!!.setOnPostListener(onPostListener)
        uiUpdate()
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode == RESULT_OK) {
                postInfo = data!!.getSerializableExtra("postinfo") as PostInfo
                contentsLayout!!.removeAllViews()
                uiUpdate()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.delete -> {
                firebaseHelper!!.storageDelete(postInfo!!)
                true
            }
            R.id.modify -> {
                myStartActivity(WritePostActivity::class.java, postInfo!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    var onPostListener: OnPostListener = object : OnPostListener {
        override fun onDelete(postInfo: PostInfo) {
            Log.e("로그 ", "삭제 성공")
        }

        override fun onModify() {
            Log.e("로그 ", "수정 성공")
        }
    }

    private fun uiUpdate() {
        setToolbarTitle(postInfo!!.title)
        readContentsVIew?.setPostInfo(postInfo!!)
    }

    private fun myStartActivity(c: Class<*>, postInfo: PostInfo) {
        val intent = Intent(this, c)
        intent.putExtra("postInfo", postInfo)
        startActivityForResult(intent, 0)
    }

}






