package com.example.flav_pof.feeds

//로그인해서 들어왔을때 창임. 여기서 로그아웃 가능하게 할거임
//클라우드firestore 데이터베이스를 통해서 로그인된 계정이 db에 있는지, db에서 데이터 읽어와서 확인함.

//이 앱은 파이어베이스를 기반으로해서 만듬. (파이어베이스는 서버리스인 db임. 이 db가 서버역할도 하는 것)
// 파이어베이스-문서-가이드-개발(인증(앱에 파이어베이스연결, 신규사용자가입 등 기능), cloud firestore(db에 저장된 회원정보 읽거나 추가 기능), storage() 등을 이용)

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import com.example.flav_pof.classes.Result_response
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.googlemap.home_map_Listener
import com.example.flav_pof.googlemap.mapFragment
import com.example.flav_pof.profileInfo.UserInfo
import com.example.flav_pof.profileInfo.UserListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.System.exit
import java.util.*


class MainActivity : BasicActivity(), home_map_Listener, OnAppointment_noexistListener {
    //전역으로 해둔 이유는 여러함수 안에서 불러와서 쓰고 싶기에. 등등
    var strNick: String? = null
    var strprofileImg: String? = null
    var strEmail: String? = null
    var mapfragment:mapFragment? = mapFragment()
    var homeFragment:HomeFragment? =null
    var userListFragment: UserListFragment? =null
    //서버로부터 가져온 내 약속목록 저장할 리스트
    var appointment_list:ArrayList<appointentInfoo> = ArrayList()
    var check_appointment:Boolean = false  //약속있는지 판별하는 변수
    //빈알림, 꽉찬알림버튼 2개 전역으로
    private lateinit var noticebutton:View
    private lateinit var noticebutton2:View

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.flav_pof.R.layout.activity_main)
        setToolbarTitle("  Foowinkle")


        //알림버튼 초기화해주고 로딩중일때 가려줌
        noticebutton = findViewById<View>(R.id.notice_button)  //알림버튼뷰를 가져옴
        noticebutton2 = findViewById<View>(R.id.notice_button2)  //알림버튼뷰를 가져옴
        noticebutton.visibility = View.GONE
        noticebutton2.visibility = View.GONE

        //kakaoLoginAct에서 보낸 인텐트를 받아서 로그인한 사용자 정보를 얻는다.
        var intent = intent
        //userId = intent.getIntExtra("id",0)  //정보가 없으면 0이 오는듯
        strNick = intent.getStringExtra("name")
        strprofileImg= intent.getStringExtra("profileImg")
        strEmail= intent.getStringExtra("email")
        Log.e(
            "태그", "main에서의 카카오,   strNick: $strNick" + "  strprofileImg: $strprofileImg" +
                    "  strEmail: $strEmail"
        )
        init()
    }


    //게시물 추가하거나 등등 할때마다 피드 갱신
    override fun onRestart() {
        super.onRestart()
        Log.e("태그", "메인액티빝의 onRestart실행")
    }


    override fun onBackPressed() {
        var builder = AlertDialog.Builder(this)
        builder.setMessage("뿌잉클을 종료할까요?")
        builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지

        builder.setPositiveButton(
            "예"
        ) { dialog, which ->  super.onBackPressed()}
        builder.setNegativeButton(
            "아니요"
        ) { dialog, which -> }

        builder.setNeutralButton(
            "취소"
        ) { dialog, which -> }
        builder.show() // 다이얼로그 보이기
    }

    //툴바 메뉴 버튼을 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_item, menu)       // toolbar_item 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바에 있는 알림 버튼 클릭시
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭시 동작을 설정한다.
        when(item.itemId){
            R.id.notice_button -> { // notice_button 알림창 버튼 클릭 시 이벤트 처리
                Log.e("태그", "알림창 클릭")
                thread_start()  //약속목록 서버로부터 가져오기 시작
                //get_myAppointmentList()
            }
            R.id.notice_button2 -> { // 알림창 버튼 클릭 시 이벤트 처리
                Log.e("태그", "notice_button2 알림창 클릭")
                thread_start()  //약속목록 서버로부터 가져오기 시작
                //get_myAppointmentList()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    //액티비티가 재실행되거나 홈버튼 눌러서 나갔다왔을때 등의 경우에 onCreate말고 이 함수가 실행됨. (이때마다 게시글들 새로고침 해주면될듯)
    //앱 처음 실행시엔 onCreate와 onResume함수가 둘다 실행되므로 중복되는 코드는 쓰지 않기
    override fun onResume() {
        super.onResume()
    } //onResume

    override fun onPause() {
        super.onPause()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> init()
        }
    }


    fun init() {
        check_appointment_list()  //약속목록 잇는지 체크해줌. 잇으면 check_appointment변수 true

         homeFragment = HomeFragment(server)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, homeFragment!!)
            .commit()

        //바텀네비게이션탭 선택에 따라 붙혀줄 fragment
        val bottomNavigationView = findViewById<BottomNavigationView>(com.example.flav_pof.R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    //상태바를 흰색으로 바꿔주는 로직
                    window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR  //글씨색은 검은색으로
                    window.statusBarColor = Color.WHITE

                    homeFragment = HomeFragment(server)  //프래그먼트가 교체될때마다 약속목록 보내줌
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, homeFragment!!)
                        .commit()
                    true
                }
                R.id.map -> {
                    //상태바를 투명하게 바꿔주는 로직
                    window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //글씨색도 투명하게 바꿔줌
                    window.statusBarColor = Color.TRANSPARENT

                    //mapfrag만 여기서 초기화 안해주는 이유는 피드에서 모든 컨텐츠값 가져온걸로 계속 맵 띄워줘야하는데, 초기화되면 값들 다 날라가니까..
                    //mapfragment = mapFragment()
                    Log.e("태그", "mapfrag로 replace")
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, mapfragment!!)
                        .commit()
                    true
                }
                R.id.userList -> {
                    //상태바를 흰색으로 바꿔주는 로직
                    window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR  //글씨색은 검은색으로
                    window.statusBarColor = Color.WHITE

                    userListFragment = UserListFragment(server)
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, userListFragment!!)
                        .commit()
                    true
                }
                else ->
                    true
            }
        }//setOnNavigationItemSelectedListener
    }  //init

    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(this, c)
        startActivityForResult(intent, 1)
    }

    //home_map_fragment를 상속받아서 implement해준 함수임. home과 map프래그먼트 사이 데이터 통신에 이용.
    override fun onCommand(map_contentsList: ArrayList<Contents>) {
        mapfragment?.display(map_contentsList)
        Log.e("태그", "메인액티비티에서 onCommand함수 실행해서 컨텐츠리스트 mapfragment에 전달")
    }

    //약속목록 서버로부터 가져오는 함수
    private fun thread_start() {
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("태그", "약속목록 가져오는 thread_start시작됨.")
    }
    fun getData() = Runnable {
        kotlin.run {
            try {
                get_myAppointmentList()  //내 약속목록 가져옴
            } catch (e: Exception) {
                Log.e("태그", "getData실패 e"+e.message)
            }
        }
    }
    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //데이터 가져오는 작업 다 끝나고 실행시킬 내용들
                Log.e("태그","약속목록 가져오는거 끝낫으면 핸들러에서 notifyDataSetChanged() 해서 업데이트")
                homeFragment?.make_appointmentSlide(appointment_list)  //자식 프래그먼트의 약속슬라이드 만드는 함수실행
            }
        }
        handler.obtainMessage().sendToTarget()
    }

    fun get_myAppointmentList() {
        appointment_list.clear()
        server.get_appointment_Request(Usersingleton.kakao_id.toString())
            .enqueue(object : Callback<Result_response> {
                override fun onFailure(call: Call<Result_response>, t: Throwable) {
                    Log.e("태그", "약속 목록 통신 아예 실패" + t.message)
                }
                override fun onResponse(
                    call: Call<Result_response>,
                    response: Response<Result_response>
                ) {
                    if (response.isSuccessful) {
                        Log.e(
                            "태그",
                            " 통신성공 -약속 목록  가져옴 ")
                        var jsonarray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonarray.length()) {
                            val Object = jsonarray.getJSONObject(i)  //각각 하나의 컨텐츠씩 가져옴
                            //following_userInfoList안에 가져오는 팔로잉 유저들 다 넣어줌
                            appointment_list!!.add( appointentInfoo(Object.getJSONObject("User").getString("username"),Object.getString("restname") ))
                            i++
                        } //repeat
                        Log.e("태그", "가져온 약속 목록: "+appointment_list)
                        handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 다음작업 수행
                    } else {
                        Log.e(
                            "태그",
                            "약속 목록 통신/ 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result.toString() + "에러: " + response.errorBody()?.string()
                                .toString()
                        )
                        handler()
                    }
                }
            })
    }

    //서버로부터 약속목록 가져와서 내용이 있는지 확인하는 함수 - 존재여부에 따라 알림버튼 모양 변경
    fun check_appointment_list(){
        appointment_list.clear()
        server.get_appointment_Request(Usersingleton.kakao_id.toString())
            .enqueue(object : Callback<Result_response> {
                override fun onFailure(call: Call<Result_response>, t: Throwable) {
                    Log.e("태그", "약속 목록 통신 아예 실패" + t.message)
                }
                override fun onResponse(
                    call: Call<Result_response>,
                    response: Response<Result_response>
                ) {
                    if (response.isSuccessful) {
                        Log.e(
                            "태그",
                            " 통신성공 -약속 목록  가져옴 ")
                        var jsonarray = JSONArray(response.body()?.result)
                        //가져온 값이 있다면 true고 없다면 false를 저장
                        check_appointment = ( jsonarray.length() != 0 )
                        Log.e("내태그", "jsonarray.length() :" +jsonarray.length())
                        Log.e("내태그", "check_appointment :" +check_appointment)

                        //알림모양 변경을 위한 작업
                        if(check_appointment){        //약속목록이 있을때
                            noticebutton.visibility = View.GONE
                            noticebutton2.visibility = View.VISIBLE
                        }else{
                            noticebutton.visibility = View.VISIBLE
                            noticebutton2.visibility = View.GONE
                        }
                    } else {
                        Log.e(
                            "태그",
                            "약속 목록 통신/ 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result.toString() + "에러: " + response.errorBody()?.string()
                                .toString()
                        )
                    }
                }
            })
    }

    //home프래그먼트에서 약속목록 다 지울때 통신함. - OnAppointment_noexistListener인터페이스의 implement한 함수
    override fun exist_appointment(delete_appointment: Boolean) {
        //홈프래그먼트에서 약속목록 삭제되어서 true반환받았다면 - 알림버튼을 빈 알림모양으로 변경
        if(delete_appointment){
            check_appointment = false  //약속있는지 체크해주는 이 변수는 false로.
            noticebutton.visibility = View.VISIBLE
            noticebutton2.visibility = View.GONE
        }

    }


}





















