package com.example.flav_pof.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.example.flav_pof.R
import com.example.flav_pof.classes.Users
import com.example.flav_pof.classes.Users_request
import com.example.flav_pof.classes.Usersingleton
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login_kakao.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class KakaoLoginActivity: BasicActivity() {

    var MainAct_Intent = Intent()  //Mainactivity로 데이터 실어서 보내줄 인텐트

    lateinit var strNick: String
    lateinit var strprofileImg: String
    lateinit var strEmail: String
    lateinit var  strkakaoid: String
    lateinit var kakao_token: String  //카카오 api접근을 위해 저장해두는 엑세스 토큰
    lateinit var user: Users //유저객체


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_kakao)
        setToolbarTitle("카카오 로그인")

        has_kakaotoken()  //카카오 토큰 있는지 판별

        //로그인 이미지 눌렀을때
        login.setOnClickListener {
            // 로그인 공통 callback 함수 - 밑에서 카톡 깔려있는지 없는지에 따라서 로그인 작업 수행해주고 그후 로그인 성공실패 작업은 여기서
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {    //오류 발생시
                    Log.e("태그", "로그인 실패", error)
                } else if (token != null) {   //토큰이 있을때 (밑에서 로그인 작업으로 생겼을때)
                    Log.e("태그", "로그인 성공 ${token.accessToken}")
                    kakao_token = token.accessToken
                    UserinfoCall_notoken()  //현재 로그인된 사용자 정보 가져옴 (이름, 이메일), 그리고 유저객체 하나 만듬
                }  //토큰 있을때 작업
            }  //콜백함수

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오 계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
                Log.e("태그", "loginWithKakaoTalk")
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                Log.e("태그", "loginWithKakaoAccount")
            }
        }  //로그인 버튼 클릭시
    } //onCrete함수


    //사용자가 아예 토큰도 없고 카톡 로그인 안되어있는 상태일때 -사용자 정보 가져와서 main에 넘겨주고 유저객체 만들어서 플레브 서버에 유저 등록.
    fun UserinfoCall_notoken() {
        // 사용자 정보 요청
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("태그", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                strNick = user.kakaoAccount?.profile?.nickname.toString()
                strEmail = user.kakaoAccount?.email.toString()
                strprofileImg = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                strkakaoid = user.id!!.toString()

                this.user = Users(strEmail, strNick, kakao_token, strkakaoid) //유저객체 하나 생성

                Usersingleton.kakao_id = user.id!!.toInt()  //유저 싱글톤에 있는 회원번호 전역변수를 초기화

                Log.e("카카오로그인화면 태그", "아예 첫 실행일때 user_id값 초기화 성공: " + Usersingleton.kakao_id)

                //main에 보내줄 회원정보 데이터 값들
                MainAct_Intent = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                MainAct_Intent.putExtra("id", user.id!!)  //회원번호
                MainAct_Intent.putExtra("name", strNick)  //프로필이름
                MainAct_Intent.putExtra("profileImg", strprofileImg)  //프로필이미지url
                MainAct_Intent.putExtra("email", strEmail)  //이메일정보 넘겨줌

                thread_start()  // 서버에 위에서 만든 신규유저 등록해주고 main화면으로 이동시키는 작업
                Log.e(
                    "태그", "UserinfoCall_notoken:  사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
            }
            Toast.makeText(this@KakaoLoginActivity, strNick + "님 환영합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //사용자가 토큰값있고 로그인 되어있는 상태라서 바로 main으로 넘어가도될때 - 유저정보 가져와서 main에 넘겨줌
    fun UserinfoCall_hastoken() {
        // 사용자 정보 요청
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("태그", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                strNick = user.kakaoAccount?.profile?.nickname.toString()
                strEmail = user.kakaoAccount?.email.toString()
                strprofileImg = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                strkakaoid = user.id!!.toString()

                Usersingleton.kakao_id = user.id!!.toInt()  //유저 싱글톤에 있는 회원번호 전역변수를 초기화

                Log.e("카카오로그인화면 태그", "토큰있고 로그인되있을때 user_id값 초기화 성공: " + Usersingleton.kakao_id)

                //main에 보내줄 회원정보 데이터 값들
                MainAct_Intent = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                //MainAct_Intent.putExtra("id", user.id!!)  //회원번호
                MainAct_Intent.putExtra("name", strNick)  //프로필이름
                MainAct_Intent.putExtra("profileImg", strprofileImg)  //프로필이미지url
                MainAct_Intent.putExtra("email", strEmail)  //이메일정보 넘겨줌

                Log.e(
                    "태그", "UserinfoCall_hastoken :   사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
            }
            startActivity(MainAct_Intent)  //main으로 유저정보 실어서 보내줌

            Toast.makeText(this@KakaoLoginActivity, strNick + "님 환영합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //처음 앱 실행하면 토큰 있는지 등등 판별해줌
    fun has_kakaotoken() {
        //카카오 토큰 있는지 판별
        if (AuthApiClient.instance.hasToken()) {  //토큰이 있을때
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {  //토큰에 오류가 있을때
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        //로그인 필요
                        Toast.makeText(
                            this@KakaoLoginActivity,
                            "error != null입니다. 로그인해주세요 ",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("태그", "UpdateKakakotalkUI/    error != null입니다. 로그인해주세요 ")
                    } else {
                        //기타 다른 토큰 에러
                        Log.e("태그", "UpdateKakakotalkUI/   기타 에러남 ")
                    }
                } else {  //토큰 이미 존재할때 (필요 시 토큰 갱신됨)
                    Log.e("태그", "has_kakaotoken함수 결과 이미 토큰값 존재. 사용자 정보값 가지고 바로 main으로 이동")
                    UserinfoCall_hastoken()
                    finish()  //카카오 로그인 액티비티 닫아줌
                }
            }
        } else {
            //로그인 필요
            Toast.makeText(this@KakaoLoginActivity, "토큰이 없습니다. 로그인 해주세요", Toast.LENGTH_SHORT).show()
            Log.e("태그", "UpdateKakakotalkUI/   토큰이 없습니다. 로그인 해주세요")
        }
    }

    //플레브 서버에 유저 등록시키는 작업
    fun user_add_Request() {
        server.user_add_Request(user).enqueue(object : Callback<Users_request> {
            override fun onFailure(call: Call<Users_request>, t: Throwable) {
                Log.e("태그", "서버 통신 아예 실패" + t.message)
            }
            override fun onResponse(call: Call<Users_request>, response: Response<Users_request>) {
                if (response.isSuccessful) {
                    Log.e(
                        "태그",
                        "통신성공" + ",  msg: " + response.body()?.msg + ",  유저id: " + response.body()?.user_id
                    )
                    handler()
                } else {
                    Log.e(
                        "태그",
                        "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.msg + "response.body(): " + response.body() + ",response.message(): " + response.errorBody()?.string()
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
                user_add_Request()  //서버에 유저 등록시켜줌
                Log.e("태그", "getData성공. 유저등록")

            } catch (e: Exception) {
                Log.e("태그", "getData실패 유저등록 실패")
            }
        }
    }

    //데이터 등록시키는 작업 다 끝났을때(성공하거나 실패했을때) 이 함수 호출해서 로딩화면 제거하는 등의 작업해주는 핸들러 함수
    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {

                Log.e("태그", "핸들러 함수 실행")
                if (strNick.isNotEmpty()) {
                    startActivity(MainAct_Intent)  //main으로 유저정보 실어서 보내줌
                    Log.e("태그", "핸들러 함수 실행햇고 유저정보 실어서 main으로 이동")
                } else {
                    Log.e("태그", "핸들러 함수 실행햇고 main으로 이동 못함")
                }
                finish()  //카카오 로그인 액티비티 닫아줌
            }
        }
        handler.obtainMessage().sendToTarget()
    }

}