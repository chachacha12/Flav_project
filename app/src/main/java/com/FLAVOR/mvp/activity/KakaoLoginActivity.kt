package com.FLAVOR.mvp.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.appIntro.AppIntroActivity
import com.FLAVOR.mvp.classes.Msg
import com.FLAVOR.mvp.classes.Users
import com.FLAVOR.mvp.classes.Users_request
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.databinding.ActivityLoginKakaoBinding
import com.FLAVOR.mvp.feeds.MainActivity
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.dialog_useragreement.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class KakaoLoginActivity: BasicActivity() {

    private lateinit var binding: ActivityLoginKakaoBinding

    var MainAct_Intent = Intent()  //Mainactivity로 데이터 실어서 보내줄 인텐트
    lateinit var strNick: String
    lateinit var strprofileImg: String
    lateinit var strEmail: String
    lateinit var strkakaoid: String
    lateinit var kakao_token: String  //카카오 api접근을 위해 저장해두는 엑세스 토큰
    lateinit var user: Users //유저객체
    var introact_check = false  //소개화면에서 온 경우인지 아닌지를 구별해줄 변수 true면 소개화면보고 온것
    private var agreement_dialog: Dialog? = null  //첨에 앱깔고 앱설명글 보고 난 후에 뜨는 앱이용약관 다이얼로그객체


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginKakaoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //로딩화면보여줌
        binding.loaderLayout.root.visibility = View.VISIBLE

        //나머지 뷰들은 가려줌
        binding.logoImage.visibility = View.INVISIBLE
        binding.loginText.visibility = View.INVISIBLE
        binding.cardViewKakaobtn.visibility = View.INVISIBLE
        setToolbarTitle("카카오 로그인")

        //앱소개화면어댑터에서 보낸 인텐트를 받아서 로그인한 사용자 정보를 얻는다.
        var intent = getIntent()
        introact_check = intent.getBooleanExtra("check", false)
        Log.e("태그", "카톡로그인 처음왓을때 바로 introact_check: " + introact_check)

        has_kakaotoken()  //카카오 토큰 있는지 판별. 없으면

        //로그인 이미지 눌렀을때
        binding.login.setOnClickListener {
            // 로그인 공통 callback 함수 - 밑에서 카톡 깔려있는지 없는지에 따라서 로그인 작업 수행해주고 그후 로그인 성공실패 작업은 여기서
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {    //오류 발생시
                    Log.e("태그", "로그인 실패", error)
                } else if (token != null) {   //토큰이 있을때 (밑에서 로그인 작업으로 생겼을때)
                    Log.e("태그", "로그인 성공 ${token.accessToken}")
                    kakao_token = token.accessToken
                    UserinfoCall_notoken()  //현재 로그인된 사용자 정보 가져옴 (이름, 이메일), 그리고 유저객체 하나 만듬
                }
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
                            "다시 로그인해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.loaderLayout.root.visibility = View.GONE

                        binding.logoImage.visibility = View.VISIBLE
                        binding.loginText.visibility = View.VISIBLE
                        binding.cardViewKakaobtn.visibility = View.VISIBLE
                    } else {
                        //기타 다른 토큰 에러
                        Log.e("태그", "UpdateKakakotalkUI/   기타 에러남 ")

                        binding.loaderLayout.root.visibility = View.GONE
                        //나머지 뷰들 다 보여줌
                        binding.logoImage.visibility = View.VISIBLE
                        binding.loginText.visibility = View.VISIBLE
                        binding.cardViewKakaobtn.visibility = View.VISIBLE
                    }
                } else {  //토큰 이미 존재할때 (필요 시 토큰 갱신됨)
                    Log.e("태그", "has_kakaotoken함수 결과 이미 토큰값 존재. 사용자 정보값 가지고 바로 main으로 이동")
                    UserinfoCall_hastoken()
                }
            }
        } else {  //토큰이 없을때는 로그인버튼 보여줌
            if (introact_check == false) {   //소개화면에 갔다온게 아닌 경우
                finish()
                //소개화면으로 보내줌.
                Log.e("태그", "introact_check == null. 앱소개화면으로 이동합니다.")
                var i = Intent(this@KakaoLoginActivity, AppIntroActivity::class.java)
                startActivity(i)
            } else {              //소개화면에 갔다가 온 경우
                Show_AgreementDialog() //이용약관 다이얼로그 실행
                //Toast.makeText(this@KakaoLoginActivity, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()

                //로그인 필요하므로 로그인 버튼 보여줌
                Log.e("태그", "UpdateKakakotalkUI/ 앱소개화면엔 갔다왔고,  토큰이 없습니다. 로그인 해주세요")
                binding.loaderLayout.root.visibility = View.GONE

                //나머지 뷰들 다 보여줌
                binding.logoImage.visibility = View.VISIBLE
                binding.loginText.visibility = View.VISIBLE
                binding.cardViewKakaobtn.visibility = View.VISIBLE


            }
        }
    }

    //사용자가 앱지웠다가 와서 다시 카톡로그인할때 또 유저등록해줘서 2번등록되는 오류 -> 서버에 등록했는데 같은값있을땐 에러로 특정값 받고 유저 update api로 유저정보 새로 수정만해줌.
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

                this.user =
                    Users(strEmail, strNick, kakao_token, strkakaoid, strprofileImg) //유저객체 하나 생성

                Usersingleton.kakao_id = user.id.toString()  //유저 싱글톤에 있는 회원번호 전역변수를 초기화
                Usersingleton.username = user.kakaoAccount?.profile?.nickname.toString()
                Usersingleton.userEmail = user.kakaoAccount?.email.toString()
                Usersingleton.profilepath = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                Log.e("싱글톤태그", "usersingleton.username: " + Usersingleton.username)

                //main에 보내줄 회원정보 데이터 값들
                MainAct_Intent = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                MainAct_Intent.putExtra("id", user.id!!)  //회원번호
                MainAct_Intent.putExtra("name", strNick)  //프로필이름
                MainAct_Intent.putExtra("profileImg", strprofileImg)  //프로필이미지url
                MainAct_Intent.putExtra("email", strEmail)  //이메일정보 넘겨줌
                MainAct_Intent.putExtra("floating_anim", true)

                //백스택 값들을 다 지워준다는뜻. 즉 이동후에 뒤로버튼 눌러도 다시 이 액티비티로 안오고 앱종료됨
                MainAct_Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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

                Usersingleton.kakao_id = user.id!!.toString()  //유저 싱글톤에 있는 회원번호 전역변수를 초기화
                Usersingleton.username = user.kakaoAccount?.profile?.nickname.toString()
                Usersingleton.userEmail = user.kakaoAccount?.email.toString()
                Usersingleton.profilepath = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
                Log.e("싱글톤태그", "usersingleton.username: " + Usersingleton.username)


                Log.e("카카오로그인화면 태그", "토큰있고 로그인되있을때 user_id값 초기화 성공: " + Usersingleton.kakao_id)

                //main에 보내줄 회원정보 데이터 값들
                MainAct_Intent = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                //MainAct_Intent.putExtra("id", user.id!!)  //회원번호
                MainAct_Intent.putExtra("name", strNick)  //프로필이름
                MainAct_Intent.putExtra("profileImg", strprofileImg)  //프로필이미지url
                MainAct_Intent.putExtra("email", strEmail)  //이메일정보 넘겨줌
                MainAct_Intent.putExtra("floating_anim", true)
                Log.e(
                    "태그", "UserinfoCall_hastoken :   사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
            }
            finish()  //카카오 로그인 액티비티 닫아줌
            startActivity(MainAct_Intent)  //main으로 유저정보 실어서 보내줌
            binding.loaderLayout.root.visibility = View.GONE
            Toast.makeText(this@KakaoLoginActivity, strNick + "님 환영합니다.", Toast.LENGTH_SHORT).show()
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
                        "통신성공" + ",  Msg: " + response.body()?.msg + ",  유저id: " + response.body()?.user_id
                    )
                    handler()
                } else {
                    if (response.errorBody()
                            ?.string() == "{\"msg\":\"ER_DUP_ENTRY\"}"
                    ) {  //이미 유저테이블에 유저 등록되어 있는경우
                        Log.e(
                            "태그",
                            "이미 등록된 유저. 카카오토큰만 갱신후 진행 response.errorBody()?.string() " + response.errorBody()
                                ?.string()
                        )
                        //유저 업데이트 해주는 로직
                        modify_kakaotoken()
                    } else {
                        Log.e(
                            "태그",
                            "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.msg + "response.body(): " + response.body() + ",response.message(): " + response.errorBody()
                                ?.string()
                        )
                    }
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

    //이미 등록된 유저가 다시 서버에 유저등록시도할때 해당 유저의 카카오토큰값만 변경해주고 새로 등록은 안해줌
    fun modify_kakaotoken() {
        server.modify_kakaotoken(user.kakao_id, user.kakaotoken!!)
            .enqueue(object : Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    Toast.makeText(this@KakaoLoginActivity, "서버 접근 실패", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {

                        Log.e("태그", "유저 카카오 토큰값 수정 성공. response.body()?.msg" + response.body()?.msg)
                    } else {
                        Log.e("태그",
                            "유저 카카오 토큰값 수정 실패. response.errorBody()?.string():" + response.errorBody()
                                ?.string()
                        )
                    }
                }
            })
    }

    //앱 이용약관 보여주는 다이얼로그
    fun Show_AgreementDialog() {
        Log.e("태그", "앱 이용약관 다이얼로그 생성")
        agreement_dialog = Dialog(this) //다이얼로그객체 초기화
        agreement_dialog!!.setContentView(R.layout.dialog_useragreement)
        agreement_dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //다이얼로그 테두리 사각형 투명하게 하기(이렇게 해야 다이얼로그 둥근테두리됨)
        agreement_dialog?.show() // 다이얼로그 띄우기
        agreement_dialog!!.setCanceledOnTouchOutside(false) //바깥을 눌러도 꺼지지않음
        agreement_dialog!!.OK_checkbutton.isEnabled = false     //최종동의 버튼비활성화

        /* 이 함수 안에 원하는 디자인과 기능을 구현하면 된다. */
        // *주의할 점: findViewById()를 쓸 때는 -> 앞에 반드시 다이얼로그 이름을 붙여야 한다.

        //개인정보처리방침 내용보기 클릭시
        agreement_dialog!!.detail_textView1.setOnClickListener {
            val i = Intent(this@KakaoLoginActivity,   PersonalInfoActivity::class.java)
            startActivity(i)
        }
        //서비스약관 내용보기 클릭시
        agreement_dialog!!.detail_textView2.setOnClickListener {
            val i = Intent(this@KakaoLoginActivity, ServiceagreementActivity::class.java)
            startActivity(i)
        }

        //두개의 체크버튼이 다 눌렸다면 최종동의버튼 활성화해줌
        agreement_dialog!!.SelfInfo_checkBox.setOnClickListener {
            if(agreement_dialog!!.SelfInfo_checkBox.isChecked && agreement_dialog!!.Serviceagreement_checkBox.isChecked) {
                agreement_dialog!!.OK_checkbutton.setBackgroundResource(R.drawable.button2_round)
                agreement_dialog!!.OK_checkbutton.isEnabled = true   //최종동의 버튼 활성화
            }else{
                agreement_dialog!!.OK_checkbutton.setBackgroundResource(R.drawable.button_round)
                agreement_dialog!!.OK_checkbutton.isEnabled = false
            }
        }
        agreement_dialog!!.Serviceagreement_checkBox.setOnClickListener {
            if(agreement_dialog!!.SelfInfo_checkBox.isChecked && agreement_dialog!!.Serviceagreement_checkBox.isChecked) {
                agreement_dialog!!.OK_checkbutton.setBackgroundResource(R.drawable.button2_round)
                agreement_dialog!!.OK_checkbutton.isEnabled = true
            }else{
                agreement_dialog!!.OK_checkbutton.setBackgroundResource(R.drawable.button_round)
                agreement_dialog!!.OK_checkbutton.isEnabled = false
            }
        }

        //최종동의버튼 활성화되었을때 클릭시
        agreement_dialog!!.OK_checkbutton.setOnClickListener {
            agreement_dialog!!.dismiss()  //다이얼로그 닫기
        }
    }



}