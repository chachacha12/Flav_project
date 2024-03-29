package com.FLAVOR.mvp.feeds

import android.R.anim
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.classes.Msg
import com.FLAVOR.mvp.classes.Result_response
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.databinding.FragmentHomeBinding
import com.FLAVOR.mvp.databinding.ViewPostBinding
import com.FLAVOR.mvp.googlemap.home_map_Listener
import com.FLAVOR.mvp.retrofit_service
import com.FLAVOR.mvp.writepost.WritePostActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//두번째 인자인 floating_anim는 유저가 앱 첫실행시 true값을 줘서 floating_TextView가 깜빡이는 애니메이션을 주기위함
class HomeFragment(var server: retrofit_service, var floating_anim:Boolean) : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //private var _binding2: ViewPostBinding? = null
   // private val binding2 get() = _binding2!!


    private var homeAdapter: HomeAdapter? = null
    //플레브 서버로부터 컨텐츠 받아오기 위한 변수들
    private var contentsList: java.util.ArrayList<Contents>? = null
    //피드의 컨텐츠들 업데이트될동안 이곳에 새로운 컨텐츠 데이터들 받고 작업 다 끝나면 기존 contentsList에 다시 addall()해줄거임
    private var update_contentsList: java.util.ArrayList<Contents>? = null
    lateinit var recyclerView:RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임
    lateinit var loaderLayout:RelativeLayout  //전역으로둬야 모든 함수에서 쓸수있어서

    //이 프래그먼트의 컨텐츠값들을 mapfragment에 주기위해 만든 인터페이스 객체
    var homeMapListener:home_map_Listener? = null
    //슬라이드업패널관련
    lateinit var slide_recyclerView:RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임
    lateinit var slidePanel: SlidingUpPanelLayout  //슬라이드업파넬레이아웃
    private var appointmentAdapter: AppointmentAdapter? = null  //올라오는 슬라이드에 밥약속목록 띄워줄 어댑터
    //약속목록 모두 삭제 버튼 눌렀을때 리사이클러뷰를 바로 업데이트 해주기위한 전역변수 리스트.main에서 값 가져온건 전역이 아니라 여기서 바로 어댑터에 넣고 업데이트 못해줘서 만듬
    var Home_appointment_list: java.util.ArrayList<appointentInfoo> = java.util.ArrayList()
    //main액티비티와 약속목록 여기서 다 지울때 통신해주는 인터페이스 객체 - 알림버튼 변경위해서.
    var onAppointment_noexistListener:OnAppointment_noexistListener? = null

    //게시물 삭제에 필요한 전역변수들
    private var successCount = 0
    private var choosen_contents_id = 0
    private var choosen_filename =""

    // 슬라이드업파넬레이아웃 이벤트 리스너
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        // 패널이 슬라이드 중일 때
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            // binding.tvSlideOffset.text = slideOffset.toString()
            Log.e("태그", "패널 슬라이드")
        }
        // 패널의 상태가 변했을 때
        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                Log.e("태그", "열기")
                // binding.btnToggle.text = "열기"
            } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                //binding.btnToggle.text = "닫기"
                Log.e("태그", "닫기")
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

      //  _binding2 = ViewPostBinding.inflate(inflater, container!!)

        val view = binding.root

        //로딩화면뷰 초기화
        loaderLayout = view.findViewById<RelativeLayout>(R.id.loaderLayout)
        loaderLayout.visibility = View.VISIBLE  //로딩화면

        contentsList = ArrayList()  //초기화  - 이거안하면 null에러남
        update_contentsList = ArrayList()  //초기화
        homeAdapter = HomeAdapter(requireActivity(), contentsList!!, server, onPostListener)  //어댑터에서도 server통신위해 server를 인자에 넣어줌
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener(
            onClickListener
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = homeAdapter
        loaderLayout.visibility = View.GONE  //로딩화면

        if(floating_anim){  //앱 첫실행한 경우라면 깜빡이는 애니메이션
            check_floatingAnimaion()
            Handler().postDelayed({
                binding.floatingTextView.visibility = View.GONE
            }, 7000)  //5초가 지났을때 {}괄호안의 내용을 수행하게되는 명령임.
        }

        slidePanel = binding.SlideUpPannerLayout  //fragment_home.xml의 가장 최상단 레이아웃을 가져옴
        slidePanel.addPanelSlideListener(PanelEventListener()) //슬라이드업파넬 이벤트 리스너 추가

        //슬라이드 열린거 내려주는 버튼
        binding.pulldownButton.setOnClickListener {
            val state = slidePanel.panelState
            // 열린 상태일 경우 닫기
            if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }
        //슬라이드 열린상태에서 약속목록 모두 지우기 버튼 클릭시
        binding.DeleteAllButton.setOnClickListener {
            if(Home_appointment_list.isEmpty()) {  //약속목록 아예 없을떄
                Toast.makeText(activity, "존재하는 약속이 없습니다.", Toast.LENGTH_SHORT).show()
                appointmentAdapter?.notifyDataSetChanged()
                //main액티비티로 빈 알림버튼으로 변경하라는 데이터를 보냄(true값) - 인터페이스 이용
                onAppointment_noexistListener?.exist_appointment(true)
            }
            else{
                delete_appointmentList()  //약속목록 지우고 appointment어댑터 업데이트, 메인액티비티로 true값 반환해서 알림버튼 변경해줌
            }
            Home_appointment_list.clear()  //약속목록 리스트 모두 비워주고 리사이클러뷰 다시 만드려고
        }
        return view
    }

    //플로팅텍스트에 애니메이션 줌
    //AlphaAnimation클래스를 이용하여 깜빡이는 애니메이션 사용
    fun check_floatingAnimaion(){
        val anim:Animation
        anim = AlphaAnimation(0.0f,1.0f)
        anim.setDuration(500) //좀 더 천천히 깜빡거리고 싶으면 이 값을 높이기  // 에니메이션 동작 주기임
        anim.setStartOffset(200)  //한번 애니메이션 끝나고 다시 시작동안 대기하는 시간
        anim.setRepeatMode(Animation.REVERSE)
        anim.setRepeatCount(15)
        binding.floatingTextView.visibility = View.VISIBLE
        binding.floatingTextView.startAnimation(anim)
    }

    //약속목록 삭제
    fun delete_appointmentList(){
        server.delete_appointmentlist_Request(Usersingleton.kakao_id.toString())
            .enqueue(object : Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable) {
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        Toast.makeText(activity, "모든 약속을 삭제했습니다.", Toast.LENGTH_SHORT).show()
                        Log.e("태그", "약속목록 삭제성공: " + response.body()?.msg)
                        appointmentAdapter?.notifyDataSetChanged()
                        //main액티비티로 빈 알림버튼으로 변경하라는 데이터를 보냄(true값) - 인터페이스 이용
                        onAppointment_noexistListener?.exist_appointment(true)
                    } else {
                        Toast.makeText(activity, "삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                        Log.e("태그", "약속목록 삭제실패: " + response.errorBody()?.string())
                    }
                }
            })
    }

    //약속리스트 보여주는 슬라이드패널 만들어서 보여줌. 부모인 main액티비티에서 실행될거임
    fun make_appointmentSlide(list: java.util.ArrayList<appointentInfoo>){
        Home_appointment_list.clear()
        Home_appointment_list.addAll(list)  //Home_appointment_list는 약속목록 모두 삭제 버튼 눌렀을때 리사이클러뷰를 바로 업데이트 해주기위한 전역변수 리스트.

        //만약 카톡친구목록에 아무도없으면 nofriendTextView를 보여줌
        if(Home_appointment_list!!.isEmpty()){
            binding.noappointmentTextView.visibility = View.VISIBLE
        }else{
            binding.noappointmentTextView.visibility = View.GONE
        }

        //리사이클러뷰 만들고 인자로 받은 약속 리스트 띄우기 로직 진행.
        appointmentAdapter = AppointmentAdapter(requireActivity(), Home_appointment_list)
        slide_recyclerView = binding.appointmentRecyclerView
        slide_recyclerView.setHasFixedSize(true)
        slide_recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        slide_recyclerView.adapter = appointmentAdapter
        //패널 열고 닫기
        val state = slidePanel.panelState
        // 닫힌 상태일 경우 열기
        if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        }else {  // 열린 상태일 경우 닫기
            slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
    }

    //mapfragment에 contents데이터를 주기위한 작업
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is home_map_Listener){ ///액티비티가 home_map_Listener타입이라면
            homeMapListener = context //액티비티를 대입해서 map리스너 초기화
        }
        if(context is OnAppointment_noexistListener){
            onAppointment_noexistListener = context
        }

    }

    override fun onDetach() {
        super.onDetach()
        if(homeMapListener != null)
            homeMapListener = null
        if(onAppointment_noexistListener != null)
            homeMapListener = null
    }

    //게시물 등 업데이트 해줄때 씀 - 게시물삭제or수정했을때 여기서 게시물 업데이트
    override fun onResume() {
        super.onResume()
        ContentsUpdate()
    }

    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.floatingActionButton -> {
                    myStartActivity(WritePostActivity::class.java)
                }
            }
        }

    //어댑터에서 특정 게시물 클릭한거 감지될때 게시물삭제, 밥약속 신청 중 하나를 해주는 인터페이스 객체
    var onPostListener: OnPostdeleteListener = object : OnPostdeleteListener {
        override fun onDelete(position: Int) {
            //s3와 rds삭제로직
            choosen_contents_id = contentsList?.get(position)?.contents_id!!  //사용자가 선택한 게시물의 id값
            choosen_filename = contentsList?.get(position)?.filename.toString()  //사용자가 선택한 게시물의 파일네임값
            storageDelete(choosen_filename)  //s3삭제로직
        }
        override fun onAppointment(position: Int) {
            //밥약속 신청로직
            var requested_kakaoid = contentsList?.get(position)?.User?.getString("kakao_id")            //선택한 컨텐츠에서 그 컨텐츠 작성자의 카카오 id값 알아오기
            var restname = contentsList?.get(position)?.restname        //선택한 컨텐츠에 있는 식당이름값 가져옴
            make_appointment(requested_kakaoid!!, restname!!)
        }
    }

    //특정 유저에게 약속신청
    fun make_appointment(requested_kakaoid: String, restname: String){
        server.make_appointment_Request(
            Usersingleton.kakao_id.toString(), requested_kakaoid, restname
        ).enqueue(object : Callback<Msg> {
            override fun onFailure(
                call: Call<Msg>,
                t: Throwable
            ) {
                Toast.makeText(activity, "밥약속 신청에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<Msg>,
                response: Response<Msg>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, "밥약속 신청 완료!", Toast.LENGTH_SHORT).show()
                } else {
                    if (response.errorBody()
                            ?.string() == "{\"msg\":\"ER_DUP_ENTRY\"}"
                    ) {   // 이미 그 게시물에 약속 신청했던 상태일때
                        Toast.makeText(activity, "이미 약속한 게시물 입니다.", Toast.LENGTH_SHORT).show()

                    } else {

                    }

                }
            }
        })
    }

    //s3스토리지 삭제로직
    fun storageDelete(filename: String) {
        successCount++  //삭제로직 시작전에
        //이미지 s3 삭제로직

        server.deleteS3_Request(Usersingleton.kakao_id!!, filename)
            .enqueue(object : Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        successCount--
                        storeDelete(choosen_contents_id)
                    } else {
                        Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        storeDelete(choosen_contents_id)
    }

    //rds삭제로직
    private fun storeDelete(contents_id: Int) {
        if (successCount == 0) {
            //게시물 삭제로직
            server.deleteContents_Request(contents_id!!)
                .enqueue(object : Callback<Msg> {
                    override fun onFailure(call: Call<Msg>, t: Throwable) {
                        Toast.makeText(activity, "삭제 실패.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                        if (response.isSuccessful) {
                            Toast.makeText(activity, "게시물을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                            ContentsUpdate()  //피드 업데이트 로직

                        } else {
                            Toast.makeText(activity, "DB에서 게시물 삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    fun ContentsUpdate() {
        //서버로부터 컨텐츠 값 가져오는 로직 + contentslist에 값 넣어주기
        thread_start()
    }

    //플레브 서버로부터 피드 가져오는 로직 -본인, 본인친구들 게시물 가져오기
    fun getRelevant_Contents_Request() {
        update_contentsList = ArrayList()  //초기화
        server.get_ReleventsContents_Request(Usersingleton.kakao_id!!)  //2번째 인자는 가져올 게시물 최대갯수
            .enqueue(object : Callback<Result_response> {
                override fun onFailure(call: Call<Result_response>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<Result_response>,
                    response: Response<Result_response>
                ) {
                    if (response.isSuccessful) {
                        var jsonarray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonarray.length()) {
                            val Object = jsonarray.getJSONObject(i)  //각각 하나의 컨텐츠씩 가져옴
                            //contentsList안에 가져오는 컨텐츠들 다 넣어줌
                            update_contentsList!!.add(
                                Contents(
                                    Object.getInt("id"),
                                    Object.getString(
                                        "date"
                                    ),
                                    Object.getString("filename"),
                                    Object.getString("filepath"),
                                    Object.getString(
                                        "restname"
                                    ),
                                    Object.getInt("user_id"),
                                    Object.getInt("adj1_id"),
                                    Object.getInt(
                                        "adj2_id"
                                    ),
                                    Object.getInt("locationtag_id"),
                                    Object.getString("lat"),
                                    Object.getString(
                                        "lng"
                                    ),
                                    Object.getString("near_station"),
                                    Object.getString("station_distance"),
                                    Object.getJSONObject(
                                        "User"
                                    ),
                                    Object.getJSONObject("Tag_FirstAdj"),
                                    Object.getJSONObject("Tag_SecondAdj"),
                                    Object.getJSONObject(
                                        "Tag_Location"
                                    ),
                                    Object.getJSONArray("Comments")
                                )
                            )
                            i++
                        } //repeat
                        handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 다음작업 수행
                    } else {
                        handler()
                    }
                }
            })
    }
    private fun thread_start() {
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                getRelevant_Contents_Request()    //서버로부터 피드 컨텐츠 가져옴

            } catch (e: Exception) {
            }
        }
    }

    //데이터 등록시키는 작업 다 끝났을때(성공하거나 실패했을때) 이 함수 호출해서 로딩화면 제거하는 등의 작업해주는 핸들러 함수
    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                contentsList?.clear()  //contentslist값 비워주기
                //contentslist에다가 새로 받아온 update_contentsList값들을 다 넣어줌
                update_contentsList?.let { contentsList?.addAll(it) }
                update_contentsList?.clear()  //피드 업데이트될때 다시 여기로 받아와야 해서 비워줌
                homeAdapter!!.notifyDataSetChanged()

                //인터페이스객체를 통해 액티비티에 있는 onCommand함수 실행-> 최종적으론 mapfragment에 데이터 전달할거임
                homeMapListener?.onCommand(contentsList!!)
            }
        }
        handler.obtainMessage().sendToTarget()
    }


    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(activity, c)
        startActivityForResult(intent, 0)
    }




}
