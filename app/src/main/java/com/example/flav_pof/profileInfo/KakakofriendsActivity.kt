package com.example.flav_pof.profileInfo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.R
import com.example.flav_pof.classes.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.talk.TalkApiClient

class KakakofriendsActivity : AppCompatActivity() {

    private val TAG = "HomeFragment"
    private var firebaseFirestore: FirebaseFirestore? = null
    private var userListAdapter: UserListAdapter? = null
    private var userList: ArrayList<UserInfo>? = null
    private var updating = false
    private var topScrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakakofriends)

        userList = ArrayList()
        userListAdapter = UserListAdapter(this, userList!!)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userListAdapter
        thread_start()
    }

    private fun postsUpdate() {
        // 카카오톡 친구 목록 가져오기 (기본)
        TalkApiClient.instance.friends { friends, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 친구 목록 가져오기 실패", error)
            } else if (friends != null) {
                Log.i(TAG, "카카오톡 친구 목록 가져오기 성공 \n${friends.elements?.joinToString("\n")}")
                // userList!!.clear()
                var i = 0
                repeat(friends.elements?.size!!) {
                    userList!!.add(
                        UserInfo(
                            friends.elements?.get(i)?.profileNickname!!,
                            friends.elements?.get(i)?.profileThumbnailImage!!
                        )
                    )
                    i++
                }
                Log.e(TAG, "userList:  " + userList.toString())
                handler()
                Log.e(TAG, "유저리스트가져오기 성공!  핸들러 실행하여 리사이클러뷰 notifiy ")
                // 친구의 UUID 로 메시지 보내기 가능
            }
        }
    }

    private fun thread_start() {
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("친구리스트 태그", "thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                postsUpdate()
                Log.e("친구리스트 태그", "getData성공. 데이터 가져옴")
            } catch (e: Exception) {
                Log.e("친구리스트 태그", "getData실패")
            }
        }
    }

    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //데이터 가져오는 작업 다 끝나고 실행시킬 내용들
                userListAdapter!!.notifyDataSetChanged()  //리사이클러뷰에 바뀐 데이터값 다시 업데이트
            }
        }
        handler.obtainMessage().sendToTarget()
    }

    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(this, c)
        startActivityForResult(intent, 0)
    }


}
