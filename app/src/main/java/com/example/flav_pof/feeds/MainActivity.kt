package com.example.flav_pof.feeds

//로그인해서 들어왔을때 창임. 여기서 로그아웃 가능하게 할거임
//클라우드firestore 데이터베이스를 통해서 로그인된 계정이 db에 있는지, db에서 데이터 읽어와서 확인함.

//이 앱은 파이어베이스를 기반으로해서 만듬. (파이어베이스는 서버리스인 db임. 이 db가 서버역할도 하는 것)
// 파이어베이스-문서-가이드-개발(인증(앱에 파이어베이스연결, 신규사용자가입 등 기능), cloud firestore(db에 저장된 회원정보 읽거나 추가 기능), storage() 등을 이용)

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import com.example.flav_pof.fragment.UserInfoFragment
import com.example.flav_pof.fragment.UserListFragment
import com.example.flav_pof.googlemap.home_map_Listener
import com.example.flav_pof.googlemap.mapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.ArrayList


class MainActivity : BasicActivity(), home_map_Listener {
    //전역으로 해둔 이유는 여러함수 안에서 불러와서 쓰고 싶기에. 등등
    private val TAG = "MainActivity"
    var strNick: String? = null
    var strprofileImg: String? = null
    var strEmail: String? = null
    var userId: Int? = null  //회원정보

    var mapfragment:mapFragment = mapFragment()
    var homeFragment:HomeFragment? =null
    var userInfoFragment:UserInfoFragment? =null
    var userListFragment:UserListFragment? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.flav_pof.R.layout.activity_main)
        setToolbarTitle("      뿌윙클")

        //kakaoLoginAct에서 보낸 인텐트를 받아서 로그인한 사용자 정보를 얻는다.
        var intent = intent
        //userId = intent.getIntExtra("id",0)  //정보가 없으면 0이 오는듯
        strNick = intent.getStringExtra("name")
        strprofileImg= intent.getStringExtra("profileImg")
        strEmail= intent.getStringExtra("email")

        Log.e("태그", "main에서의 카카오,   strNick: $strNick"+ "  strprofileImg: $strprofileImg"+
            "  strEmail: $strEmail")
        //getHashKey()
        init()
    }


    //게시물 추가하거나 등등 할때마다 피드 갱신
    override fun onRestart() {
        super.onRestart()
        Log.e("태그","메인액티빝의 onRestart실행" )
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    //툴바 메뉴 버튼을 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_item, menu)       // toolbar_item 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 콜백
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭시 동작을 설정한다.
        when(item!!.itemId){
             R.id.notice_button->{ // 알림창 버튼 클릭 시 이벤트 처리
                Log.e("태그","알림창 클릭")
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

        /*
        val homefragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, HomeFragment::class.java.name)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, homefragment)
            .commit()
        Log.e("태그","메인액티비티에서 fragmentfactory써서 homefragment를 commit")
         */

        var homeFragment = HomeFragment(server)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, homeFragment)
            .commit()

        //바텀네비게이션탭 선택에 따라 붙혀줄 fragment
        val bottomNavigationView = findViewById<BottomNavigationView>(com.example.flav_pof.R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener {

            when(it.itemId) {
                R.id.home -> {
                     homeFragment = HomeFragment(server)
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, homeFragment)
                        .commit()
                    true
                }
                R.id.myInfo -> {
                    userInfoFragment = UserInfoFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, userInfoFragment!!)
                        .commit()
                    true
                }
                R.id.userList -> {
                     userListFragment = UserListFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, userListFragment!!)
                        .commit()
                    true
                }
                R.id.map -> {
                    //mapfragment = mapFragment()
                    Log.e("태그", "mapfrag로 replace")
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.flav_pof.R.id.container, mapfragment!!)
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
        Log.e("태그","메인액티비티에서 onCommand함수 실행해서 컨텐츠리스트 mapfragment에 전달")
    }


}





















