package com.example.flav_pof.activity

//로그인해서 들어왔을때 창임. 여기서 로그아웃 가능하게 할거임
//클라우드firestore 데이터베이스를 통해서 로그인된 계정이 db에 있는지, db에서 데이터 읽어와서 확인함.

//이 앱은 파이어베이스를 기반으로해서 만듬. (파이어베이스는 서버리스인 db임. 이 db가 서버역할도 하는 것)
// 파이어베이스-문서-가이드-개발(인증(앱에 파이어베이스연결, 신규사용자가입 등 기능), cloud firestore(db에 저장된 회원정보 읽거나 추가 기능), storage() 등을 이용)

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.flav_pof.fragment.HomeFragment
import com.example.flav_pof.fragment.UserInfoFragment
import com.example.flav_pof.fragment.UserListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BasicActivity() {
    //전역으로 해둔 이유는 여러함수 안에서 불러와서 쓰고 싶기에. 등등
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.flav_pof.R.layout.activity_main)
        setToolbarTitle("뿌윙클")

        init()
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
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {//만약 현재 유저가 null이면... (즉, 로그인이 아직 안되어있다는 뜻)
            myStartActivity(SignUpActivity::class.java ) //회원가입창 화면으로 이동
        } else {
            val documentReference =
                FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
            documentReference.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document: DocumentSnapshot? = task.result
                        if (document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.data)
                            } else {
                                Log.d(TAG, "No such document")
                                myStartActivity(UserInitActivity::class.java)
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.exception)
                    }
                }

            val homeFragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(com.example.flav_pof.R.id.container, homeFragment)
                .commit()

            val bottomNavigationView = findViewById<BottomNavigationView>(com.example.flav_pof.R.id.bottomNavigationView)
            bottomNavigationView.setOnNavigationItemSelectedListener {

                when(it.itemId) {
                    com.example.flav_pof.R.id.home -> {
                        val homeFragment = HomeFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(com.example.flav_pof.R.id.container, homeFragment)
                            .commit()
                        true
                    }
                    com.example.flav_pof.R.id.myInfo -> {
                        val userInfoFragment = UserInfoFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(com.example.flav_pof.R.id.container, userInfoFragment)
                            .commit()
                        true
                    }
                    com.example.flav_pof.R.id.userList -> {
                        val userListFragment = UserListFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(com.example.flav_pof.R.id.container, userListFragment)
                            .commit()
                        true
                    }
                    else ->
                        true
                }
            }//setOnNavigationItemSelectedListener

        }//else

    }  //init

    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(this, c)
        startActivityForResult(intent, 1)
    }


}





















