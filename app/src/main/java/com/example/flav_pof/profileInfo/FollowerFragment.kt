package com.example.flav_pof.profileInfo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.flav_pof.R
import com.example.flav_pof.classes.Result_response
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.feeds.Contents
import com.example.flav_pof.retrofit_service
import com.google.firebase.firestore.auth.User
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//팔로잉 리스트 보여주는 프래그먼트
class FollowerFragment(var server: retrofit_service) : Fragment() {

    private var followerAdapter: FollowerAdapter? = null

    //플레브 서버로부터 팔로잉 목록 받아오기 위한 변수들
    private var follower_userInfoList: java.util.ArrayList<UserInfo>? = null

    //팔로잉 목록들 업데이트될동안 이곳에 새로운 컨텐츠 데이터들 받고 작업 다 끝나면 기존 following_userInfoList에 다시 addall()해줄거임
    private var update_follower_userInfoList: java.util.ArrayList<UserInfo>? = null
    lateinit var recyclerView: RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임
    //lateinit var loaderLayout: RelativeLayout  //전역으로둬야 모든 함수에서 쓸수있어서

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_follower, container, false)

        //로딩화면뷰 초기화
        //loaderLayout = view.findViewById<RelativeLayout>(R.id.loaderLayout)
        // loaderLayout.visibility = View.VISIBLE  //로딩화면

        follower_userInfoList = ArrayList()  //초기화  - 이거안하면 null에러남
        update_follower_userInfoList = ArrayList()  //초기화

        followerAdapter = FollowerAdapter(requireActivity(), follower_userInfoList!!)  //어댑터에서도 server통신위해 server를 인자에 넣어줌
        recyclerView = view.findViewById<RecyclerView>(R.id.follower_recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = followerAdapter

        //loaderLayout.visibility = View.GONE  //로딩화면
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thread_start()  //서버로부터 팔로잉 목록 가져오기 시작
    }

    //게시물 등 업데이트 해줄떄 씀 - 게시물삭제or수정했을때 여기서 게시물 업데이트
    override fun onResume() {
        super.onResume()
        Log.e("태그", "팔로워프래그먼트에서 onResume이 실행됨")
    }


    //서버에서 내 팔로잉 목록 가져옴
    fun get_myfollower_Request(){
        update_follower_userInfoList = ArrayList()  //초기화

        server.get_follower_Request(Usersingleton.kakao_id.toString())
            .enqueue(object : Callback<Result_response> {
                override fun onFailure(call: Call<Result_response>, t: Throwable) {
                    Log.e("태그", "팔로워 목록 통신 아예 실패" + t.message)
                }
                override fun onResponse(
                    call: Call<Result_response>,
                    response: Response<Result_response>
                ) {
                    if (response.isSuccessful) {
                        Log.e(
                            "태그",
                            " 통신성공 - 팔로워 목록  가져옴 ")
                        var jsonarray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonarray.length()) {
                            val Object = jsonarray.getJSONObject(i)  //각각 하나의 컨텐츠씩 가져옴
                            //following_userInfoList안에 가져오는 팔로잉 유저들 다 넣어줌
                            update_follower_userInfoList!!.add( UserInfo(Object.getString("username"),Object.getString("profileimg_path"),
                                Object.getInt("id").toString()   ))
                            i++
                        } //repeat
                        Log.e("태그", "가져온 팔로워 목록: "+update_follower_userInfoList)
                        handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 다음작업 수행
                    } else {
                        Log.e(
                            "태그",
                            "팔로워 목록 통신/ 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result.toString() + "에러: " + response.errorBody()?.string()
                                .toString()
                        )
                        handler()
                    }
                }
            })

    }

    private fun thread_start() {
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("태그", "thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                get_myfollower_Request()    //서버로부터 팔로잉목록 가져옴

            } catch (e: Exception) {

            }
        }
    }

    //데이터 등록시키는 작업 다 끝났을때(성공하거나 실패했을때) 이 함수 호출해서 로딩화면 제거하는 등의 작업해주는 핸들러 함수
    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                Log.e("태그", "팔로워 목록 다 get한 후에 지금 핸들러 함수 실행")
                follower_userInfoList?.clear()  //following_userInfoList값 비워주기
                //following_userInfoList에다가 새로 받아온 update_following_userInfoList값들을 다 넣어줌
                update_follower_userInfoList?.let { follower_userInfoList?.addAll(it) }
                update_follower_userInfoList?.clear()  //피드 업데이트될때 다시 여기로 받아와야 해서 비워줌
                followerAdapter!!.notifyDataSetChanged()
            }
        }
        handler.obtainMessage().sendToTarget()
    }


}
