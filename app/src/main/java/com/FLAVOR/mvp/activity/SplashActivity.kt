package com.FLAVOR.mvp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.FLAVOR.mvp.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //텍스트뷰에 애니메이션을 적용함.
        var textanim = AnimationUtils.loadAnimation(this,R.anim.animation_splash_text)
        splash_textView.animation = textanim

        Handler().postDelayed({
            val intent = Intent(this, KakaoLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        },1500)
    }

}
