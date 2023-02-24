package com.FLAVOR.mvp.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.databinding.ActivityGalleryBinding
import com.FLAVOR.mvp.databinding.ActivityLoginKakaoBinding
import com.FLAVOR.mvp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //텍스트뷰에 애니메이션을 적용함.
        var textanim = AnimationUtils.loadAnimation(this,R.anim.animation_splash_text)

        binding.splashTextView.animation = textanim


        Handler().postDelayed({
            val intent = Intent(this, KakaoLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        },1500)
    }

}
