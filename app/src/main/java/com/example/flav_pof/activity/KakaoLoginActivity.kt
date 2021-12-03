package com.example.flav_pof.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import com.example.flav_pof.R
import com.example.flav_pof.classes.Name
import com.example.flav_pof.classes.Users
import com.example.flav_pof.classes.Users_request
import com.example.flav_pof.retrofit_service
import com.kakao.auth.AuthType
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.usermgmt.response.model.Profile
import com.kakao.util.OptionalBoolean
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.activity_login_kakao.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class KakaoLoginActivity: BasicActivity() {

    var MainAct_Intent = Intent()  //Mainactivity로 데이터 실어서 보내줄 인텐트

    lateinit var strNick: String
    lateinit var strprofileImg: String
    lateinit var strEmail: String
    lateinit var user:Users //유저객체

    //카톡 로그인 관리하는 객체
    private lateinit var sessionCallback:ISessionCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_kakao)
        setToolbarTitle("카카오 로그인")

        //카톡 로그인 관리하는 객체
        sessionCallback = object: ISessionCallback{
            override fun onSessionOpened() {
                //여기서 실제 로그인 요청을 함
                requestMe()
            }
            override fun onSessionOpenFailed(exception: KakaoException?) {
                Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception?.message)
            }

        }

        Session.getCurrentSession().addCallback(sessionCallback)
        //이 구문이 있으면 일단 세션이 유지되고 있어서 별도로 로그아웃 안하면 자동로그인 될거임.
        Session.getCurrentSession().checkAndImplicitOpen()
    }


    override fun onDestroy() {
        super.onDestroy()

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    // 사용자 정보 요청
    private fun requestMe() {
        UserManagement.getInstance()
            .me(object : MeV2ResponseCallback() {
                override fun onSessionClosed(errorResult: ErrorResult) {
                    //세션닫힘
                    Log.e("KAKAO_API", "세션이 닫혀 있음: $errorResult")
                }

                override fun onFailure(errorResult: ErrorResult) {
                    //요청했지만 로그인 실패했을시
                    Log.e("KAKAO_API", "사용자 정보 요청 실패: $errorResult")
                    Toast.makeText(this@KakaoLoginActivity, "로그인 도중에 오류가 발생했습니다.",Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(result: MeV2Response) {
                    //요청해서 로그인 성공.여기서 이제 인텐트로 다른 액티비티로 이동하거나 등등 해주면 된다
                    //로그인 성공시 카카오에서 result라는 객체를 던져줌. result를 이용해서 카카오 디벨로퍼-동의항목에서 받을 사용자 정보들 데이터를 가져올 수 있음

                    //유저이름, 이메일 전역변수를 초기화
                    strNick = result.kakaoAccount.profile.nickname
                    strEmail = result.kakaoAccount.email
                    user = Users(strEmail,strNick) //유저객체 하나 생성

                    thread_start()  //서버에 유저 등록시켜주는 스레드 생성

                    Toast.makeText(this@KakaoLoginActivity, "환영합니다!",Toast.LENGTH_SHORT).show()

                    MainAct_Intent = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                    MainAct_Intent.putExtra("name",result.kakaoAccount.profile.nickname)  //프로필이름
                    MainAct_Intent.putExtra("profileImg", result.kakaoAccount.profile.profileImageUrl)  //프로필이미지url
                    MainAct_Intent.putExtra("email", result.kakaoAccount.email)  //이메일정보 넘겨줌

                    Log.e("kakologin에서의 카카오", "  strNick: $strNick"+ "  strprofileImg: "+result.kakaoAccount.profile.profileImageUrl+
                            "  strEmail: $strEmail")
                }
            })
    }

    //서버에 유저 등록시키는 작업
    fun user_add_Request(){
        server.user_add_Request(user).enqueue(object : Callback<Users_request> {
            override fun onFailure(call: Call<Users_request>, t: Throwable) {
                Log.e("태그", "서버 통신 아예 실패" + t.message)
            }
            override fun onResponse(call: Call<Users_request>, response: Response<Users_request>) {
                if (response.isSuccessful) {
                    Log.e("태그", "통신성공"+",  msg: " + response.body()?.msg+",  유저id: "+response.body()?.user_id)
                    handler()
                } else {
                    Log.e(
                        "태그",
                        "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.msg + "response.body(): " + response.body() +",response.message(): "+response.message()
                    )
                    handler()
                }
            }
        })
    }

    private fun thread_start(){
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("태그","thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                user_add_Request()  //서버에 유저 등록시켜줌
                Log.e("태그","getData성공. 유저등록")

            }catch (e:Exception){
                Log.e("태그","getData실패 유저등록 실패")
            }
        }
    }

    //데이터 등록시키는 작업 다 끝났을때(성공하거나 실패했을때) 이 함수 호출해서 로딩화면 제거하는 등의 작업해주는 핸들러 함수
    private fun handler(){
        var handler = object: Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {

                Log.e("태그","핸들러 함수 실행")
                if(strNick.isNotEmpty()){
                    startActivity(MainAct_Intent)  //main으로 유저정보 실어서 보내줌
                    Log.e("태그","핸들러 함수 실행햇고 유저정보 실어서 main으로 이동")
                }else{
                    Log.e("태그","핸들러 함수 실행햇고 main으로 이동 못함")
                }
                finish()  //카카오 로그인 액티비티 닫아줌
            }
        }
        handler.obtainMessage().sendToTarget()
    }







}