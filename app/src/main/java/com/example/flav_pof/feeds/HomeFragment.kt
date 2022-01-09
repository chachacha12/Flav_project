package com.example.flav_pof.feeds

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.classes.Msg
import com.example.flav_pof.classes.Result_response
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.googlemap.home_map_Listener
import com.example.flav_pof.retrofit_service
import com.example.flav_pof.writepost.WritePostActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.view_loader.*
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

    private var homeAdapter: HomeAdapter? = null
    private var topScrolled = false

    //플레브 서버로부터 컨텐츠 받아오기 위한 변수들
    private var contentsList: java.util.ArrayList<Contents>? = null
    //피드의 컨텐츠들 업데이트될동안 이곳에 새로운 컨텐츠 데이터들 받고 작업 다 끝나면 기존 contentsList에 다시 addall()해줄거임
    private var update_contentsList: java.util.ArrayList<Contents>? = null
    lateinit var recyclerView:RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임
    lateinit var loaderLayout:RelativeLayout  //전역으로둬야 모든 함수에서 쓸수있어서

    //이 프래그먼트의 컨텐츠값들을 mapfragment에 주기위해 만든 인터페이스 객체
    var homeMapListener:home_map_Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        //로딩화면뷰 초기화
        loaderLayout = view.findViewById<RelativeLayout>(R.id.loaderLayout)
        loaderLayout.visibility = View.VISIBLE  //로딩화면

        contentsList = ArrayList()  //초기화  - 이거안하면 null에러남
        update_contentsList = ArrayList()  //초기화

        homeAdapter = HomeAdapter(requireActivity(), contentsList!!,server, onPostListener)  //어댑터에서도 server통신위해 server를 인자에 넣어줌

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener(onClickListener)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = homeAdapter

        /*
         recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //맨위에서 스크롤 할때마다 피드 컨텐츠 새로 업데이트 해주는 로직
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager
                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

                if (newState == 1 && firstVisibleItemPosition == 0) {
                    topScrolled = true
                }
                if (newState == 0 && topScrolled) {
                    ContentsUpdate()
                    Log.e("ContentsUpdate태그", "위로 스크롤해서 -- ContentsUpdate진행 ")
                    topScrolled = false
                }
            }
            /*
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
             */
        })
         */
        loaderLayout.visibility = View.GONE  //로딩화면
        return view
    }

    //mapfragment에 contents데이터를 주기위한 작업
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is home_map_Listener){ ///액티비티가 home_map_Listener타입이라면
            homeMapListener = context //액티비티를 대입해서 map리스너 초기화
        }
    }
    override fun onDetach() {
        super.onDetach()
        if(homeMapListener != null)
            homeMapListener = null
    }



    //게시물 등 업데이트 해줄떄 씀 - 게시물삭제or수정했을때 여기서 게시물 업데이트
    override fun onResume() {
        super.onResume()
        Log.e("태그", "homefragment에서 onResume이 실행됨")
        ContentsUpdate()
        Log.e("ContentsUpdate태그", "onResume -- ContentsUpdate진행 ")
    }

    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.floatingActionButton ->  { myStartActivity(WritePostActivity::class.java)}
            }
        }

    //게시물 삭제에 필요한 전역변수들
    private var successCount = 0
    private var choosen_contents_id = 0
    private var choosen_filename =""

    //firebasehelper 클래스 안쓰고이 안에서 게시물 삭제로직 다 짤거임. 그래야 업데이트하기 편해서
    var onPostListener: OnPostListener = object : OnPostListener {
        //override fun onDelete(postInfo: PostInfo)
        override fun onDelete(position: Int) {
            //s3와 rds삭제로직
            choosen_contents_id = contentsList?.get(position)?.contents_id!!  //사용자가 선택한 게시물의 id값
            choosen_filename = contentsList?.get(position)?.filename.toString()  //사용자가 선택한 게시물의 id값

            storageDelete(choosen_filename!!)  //s3삭제로직
        }
        override fun onModify() {
            Log.e("로그: ", "수정 성공")
        }
    }

    //s3스토리지 삭제로직
    fun storageDelete(filename: String) {
        successCount++  //삭제로직 시작전에
        //이미지 s3 삭제로직
        Log.e("태그", "s3 삭제시 필요한 인자들 Usersingleton.kakao_id, contents.filename : "+Usersingleton.kakao_id+" ,"+choosen_filename)
        server.deleteS3_Request( Usersingleton.kakao_id!!, filename)
            .enqueue(object : Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                    Log.e("삭제태그", "s3 삭제실패 - 통신 아예 실패")
                }
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        successCount--
                        storeDelete(choosen_contents_id)
                        Log.e("삭제태그", "s3 삭제성공")
                    } else {
                        Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                        Log.e("삭제태그", "서버 접근했지만 s3 삭제실패: "+response.body()?.msg)
                    }
                }
            })
        storeDelete(choosen_contents_id)
    }

    //rds삭제로직
    private fun storeDelete(contents_id: Int) {
        if (successCount == 0) {
            //게시물 삭제로직
            server.deleteContents_Request( contents_id!!)
                .enqueue(object : Callback<Msg> {
                    override fun onFailure(call: Call<Msg>, t: Throwable) {
                        Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                        Log.e("삭제태그", "rds 삭제 통신 아예 실패")
                    }
                    override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                        if (response.isSuccessful) {
                            Toast.makeText(activity, "게시물을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("삭제태그", "rds 삭제성공: ")
                            ContentsUpdate()  //피드 업데이트 로직
                            Log.e("ContentsUpdate태그", "게시물 삭제해서 --ContentsUpdate진행 ")

                        } else {
                            Toast.makeText(activity, "DB에서 게시물 삭제 실패", Toast.LENGTH_SHORT).show()
                            Log.e("삭제태그", "rds 삭제실패: "+response.body()?.msg)
                        }
                    }
                })
        }

    }


    fun ContentsUpdate() {
        Log.e("태그","홈프래그먼트에서 피드 가져올때 Usersingleton.kakao_id: "+Usersingleton.kakao_id)
        //서버로부터 컨텐츠 값 가져오는 로직 + contentslist에 값 넣어주기
        thread_start()
    }

    //플레브 서버로부터 피드 가져오는 로직
    fun getRelevant_Contents_Request() {
        update_contentsList = ArrayList()  //초기화

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
                            " 통신성공 - 피드에 컨텐츠 채워줌 ")
                        var jsonarray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonarray.length()) {
                            val Object = jsonarray.getJSONObject(i)  //각각 하나의 컨텐츠씩 가져옴
                            //contentsList안에 가져오는 컨텐츠들 다 넣어줌
                            update_contentsList!!.add( Contents( Object.getInt("id"), Object.getString("date"),
                                Object.getString("filename"),  Object.getString("filepath"),  Object.getString("restname"),
                                Object.getInt("user_id"),  Object.getInt("adj1_id"),  Object.getInt("adj2_id"),
                                Object.getInt("locationtag_id"),  Object.getString("lat"), Object.getString("lng"),
                                Object.getString("near_station"), Object.getString("station_distance"), Object.getJSONObject("User"),
                                Object.getJSONObject("Tag_FirstAdj"), Object.getJSONObject("Tag_SecondAdj"),Object.getJSONObject("Tag_Location")
                            )  )
                            i++
                        } //repeat
                        handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 다음작업 수행
                    } else {
                        Log.e(
                            "관련 컨텐츠 태그",
                            "관련 컨텐츠 / 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result.toString() + "에러: " + response.errorBody()?.string()
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
                contentsList?.clear()  //contentslist값 비워주기
                //contentslist에다가 새로 받아온 update_contentsList값들을 다 넣어줌
                update_contentsList?.let { contentsList?.addAll(it) }
                update_contentsList?.clear()  //피드 업데이트될때 다시 여기로 받아와야 해서 비워줌
                homeAdapter!!.notifyDataSetChanged()

                //인터페이스객체를 통해 액티비티에 있는 onCommand함수 실행-> 최종적으론 mapfragment에 데이터 전달할거임
                homeMapListener?.onCommand(contentsList!!)
                Log.e("태그","홈프래그먼트에서 액티비티로 컨텐츠리스트 줌")
            }
        }
        handler.obtainMessage().sendToTarget()
    }


    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(activity, c)
        startActivityForResult(intent, 0)
    }




}
