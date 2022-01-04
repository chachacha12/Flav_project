package com.example.flav_pof.feeds

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
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.classes.Result_response
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.retrofit_service
import com.example.flav_pof.writepost.WritePostActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment(var server:retrofit_service) : Fragment() {
    private val TAG = "HomeFragment"
    private var firebaseFirestore: FirebaseFirestore? = null
    private var homeAdapter: HomeAdapter? = null
    private var postList: ArrayList<PostInfo>? = null
    private var updating = false
    private var topScrolled = false


    //플레브 서버로부터 컨텐츠 받아오기 위한 변수들
    private var contentsList: java.util.ArrayList<Contents>? = null

    lateinit var recyclerView:RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseFirestore = FirebaseFirestore.getInstance()
        //postList = ArrayList()
       // homeAdapter = HomeAdapter(requireActivity(), postList!!)
        contentsList = ArrayList()
        homeAdapter = HomeAdapter(requireActivity(), contentsList!!)
        homeAdapter!!.setOnPostListener(onPostListener)


        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener(onClickListener)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        //recyclerView.adapter = homeAdapter


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager
                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

                if (newState == 1 && firstVisibleItemPosition == 0) {
                    topScrolled = true
                }
                if (newState == 0 && topScrolled) {
                    ContentsUpdate(true)

                    topScrolled = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                val visibleItemCount = layoutManager!!.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                val lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                if (totalItemCount - 3 <= lastVisibleItemPosition && !updating) {
                    ContentsUpdate(false)

                }
                if (0 < firstVisibleItemPosition) {
                    topScrolled = false
                }
            }
        })

        ContentsUpdate(false)

        return view
    }


    override fun onDetach() {
        super.onDetach()
    }


    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.floatingActionButton ->  { myStartActivity(WritePostActivity::class.java)}
                /*
                               case R.id.logoutButton:
                                   FirebaseAuth.getInstance().signOut();
                                   myStartActivity(SignUpActivity.class);
                                   break;
                               */
            }
        }

    var onPostListener: OnPostListener = object : OnPostListener {
        //override fun onDelete(postInfo: PostInfo)
        override fun onDelete(contents: Contents) {
            //postList!!.remove(postInfo)
            contentsList!!.remove(contents)

            homeAdapter!!.notifyDataSetChanged()
            Log.e("로그: ", "삭제 성공")
        }

        override fun onModify() {
            Log.e("로그: ", "수정 성공")
        }
    }


    private fun ContentsUpdate(clear: Boolean) {
        updating = true
        val date: String =
            if (contentsList!!.size == 0 || clear)
                Date().toString()
            else
                contentsList!![contentsList!!.size - 1].date

        Log.e("태그","홈프래그먼트에서 피드 가져올때 Usersingleton.kakao_id: "+Usersingleton.kakao_id)
        //서버로부터 컨텐츠 값 가져오는 로직 + contentslist에 값 넣어주기
        thread_start()

    }


    //플레브 서버로부터 피드 가져오는 로직
    fun getRelevant_Contents_Request() {
        server.get_ReleventsContents_Request(Usersingleton.kakao_id!!)
            .enqueue(object : Callback<Result_response> {
                override fun onFailure(call: Call<Result_response>, t: Throwable) {
                    Log.e("관련 컨텐츠 태그", "피드 컨텐츠 서버 통신 아예 실패" + t.message)
                }
                override fun onResponse(
                    call: Call<Result_response>,
                    response: Response<Result_response>
                ) {
                    if (response.isSuccessful) {
                        Log.e(
                            "관련 컨텐츠 태그",
                            "관련 컨텐츠 / 통신성공" + response.body()?.result.toString()
                        )

                        var jsonarray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonarray.length()) {
                            val Object = jsonarray.getJSONObject(i)  //각각 하나의 컨텐츠씩 가져옴
                            //contentsList안에 가져오는 컨텐츠들 다 넣어줌
                            contentsList!!.add( Contents( Object.getInt("id"), Object.getString("date"),
                                Object.getString("filename"),  Object.getString("filepath"),  Object.getString("restname"),
                                Object.getInt("user_id"),  Object.getInt("adj1_id"),  Object.getInt("adj2_id"),
                                Object.getInt("locationtag_id"),  Object.getString("lat"), Object.getString("lng"),
                                Object.getString("near_station"), Object.getString("station_distance")
                            )  )

                            Log.e(
                                "관련 컨텐츠 태그",
                                "Object.getString으로.. 반복문안의 restname: " + Object.getString("restname"
                            ))
                            i++
                        } //repeat
                        //homeAdapter!!.notifyDataSetChanged()
                        Log.e(
                            "관련 컨텐츠 태그",
                            "contentsList" + contentsList.toString()
                        )

                        handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 다음작업 수행
                    } else {
                        Log.e(
                            "관련 컨텐츠 태그",
                            "관련 컨텐츠 / 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result.toString() + "에러: " + response.errorBody()?.string()
                                .toString()
                        )
                        handler()
                    }
                    updating = false
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
                getRelevant_Contents_Request()    //서버로부터 피드 컨텐츠 가져옴
                Log.e("태그", "getRelevant_Contents_Request 성공 . ")

            } catch (e: Exception) {
                Log.e("태그", "getRelevant_Contents_Request 실패 ")
            }
        }
    }

    //데이터 등록시키는 작업 다 끝났을때(성공하거나 실패했을때) 이 함수 호출해서 로딩화면 제거하는 등의 작업해주는 핸들러 함수
    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                Log.e("태그", "피드컨텐츠 다 get한 후에 지금 핸들러 함수 실행")
                recyclerView.adapter = homeAdapter
            }
        }
        handler.obtainMessage().sendToTarget()
    }



    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(activity, c)
        startActivityForResult(intent, 0)
    }




}
