package com.example.flav_pof.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import com.example.flav_pof.R
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




class KakaoLoginActivity: BasicActivity() {

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

                    Toast.makeText(this@KakaoLoginActivity, "환영합니다!",Toast.LENGTH_SHORT).show()

                    var i = Intent(this@KakaoLoginActivity, MainActivity::class.java)
                    intent.putExtra("name",result.kakaoAccount.profile.nickname)  //프로필이름
                    intent.putExtra("profileImg", result.kakaoAccount.profile.profileImageUrl)  //프로필이미지url
                    intent.putExtra("email", result.kakaoAccount.email)  //이메일정보 넘겨줌
                    startActivity(i)

                }
            })
    }



}