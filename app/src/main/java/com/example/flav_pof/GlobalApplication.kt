package com.example.flav_pof

import android.app.Application
import com.kakao.sdk.common.KakaoSdk


//카카오 api연동을 위한 클래스s
//카카오 sdk를 사용하기 위해서는 초기화 해줘야함.
//초기화는 이 GlobalApplication 공유 클래스를 만들어 앱 수준에서 관리하도록 할거임


class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //instance = this

        // Kakao SDK 초기화
        KakaoSdk.init(this, "c96f6bd57c1d1f50be96460adb9705c3")
    }

    override fun onTerminate() {
        super.onTerminate()
       // instance = null
    }


    /*
    inner class KakaoSDKAdapter : KakaoAdapter() {

        //카카오 로그인 세션을 불러올 때의 설정값을 설정하는 부분
        override fun getSessionConfig(): ISessionConfig {
            return object : ISessionConfig {

                override fun getAuthTypes(): Array<AuthType> {
                    return arrayOf(AuthType.KAKAO_LOGIN_ALL)
                    //로그인을 어떤 방식으로 할지 지정
                    //KAKAO_LOGIN_ALL: 모든 로그인 방식을 사용하고 싶을때 지정
                }

                override fun isUsingWebviewTimer(): Boolean {
                    return false
                }

                override fun isSecureMode(): Boolean {
                    return false
                }

                override fun getApprovalType(): ApprovalType? {
                    return ApprovalType.INDIVIDUAL
                }

                override fun isSaveFormData(): Boolean {
                    return true
                }
            }
        }

        // Application이 가지고 있는 정보를 얻기 위한 인터페이스
        override fun getApplicationConfig(): IApplicationConfig {
            return IApplicationConfig { globalApplicationContext }
        }
    }

    companion object {
        private var instance: GlobalApplication? = null
        val globalApplicationContext: GlobalApplication?
            get() {
                checkNotNull(instance) { "This Application does not inherit com.kakao.GlobalApplication" }
                return instance
            }
    }

     */
}