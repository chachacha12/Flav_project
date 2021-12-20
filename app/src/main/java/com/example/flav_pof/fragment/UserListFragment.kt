package com.example.flav_pof.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.Adapter.HomeAdapter
import com.example.flav_pof.Adapter.UserListAdapter
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.UserInfo
import com.example.flav_pof.activity.WritePostActivity
import com.example.flav_pof.classes.Name
import com.example.flav_pof.listener.OnPostListener
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.talk.TalkApiClient
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 */
class UserListFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var firebaseFirestore: FirebaseFirestore? = null
    private var userListAdapter: UserListAdapter? = null
    private var userList: ArrayList<UserInfo>? = null
    private var updating = false
    private var topScrolled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_user_list, container, false)

        userList = ArrayList()
        userListAdapter = UserListAdapter(requireActivity(), userList!!)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = userListAdapter

        thread_start()

        return view
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
        val intent = Intent(activity, c)
        startActivityForResult(intent, 0)
    }
}
