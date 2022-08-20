package com.FLAVOR.mvp.activity    //부모클래스(액티비티)임
// 모든 액티비티에 공통으로 필요한 코드를 이 클래스에 넣고 다른 액티비티에서 이 클래스를 상속받을거임
//다른 액티비티들 만들고 클래스이름옆 : 에다가 BasicActivity쓰면 상속됨

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.retrofit_service
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


open class BasicActivity : AppCompatActivity() {
    
    var retrofit = Retrofit.Builder()
        .baseUrl( "https://api.foowinkle.social/" )  //도메인주소값
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var server = retrofit.create(retrofit_service::class.java)  //서버와 만들어둔 인터페이스를 연결시켜줌.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //화면의 가로세로 관련 문제 해결을 위해..
    }


    //모든 액티비티에 툴바를 달아주기 위해 basicact에서 달아줌
    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        val myToolbar = findViewById<androidx.appcompat.widget.Toolbar>(com.FLAVOR.mvp.R.id.toolbar)
        setSupportActionBar(myToolbar)
    }

    open fun setToolbarTitle(title: String?) {
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(title)
        }
    }

    //툴바색 변경
    open fun setToolbarbackground(color: Color?) {
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
        }
    }



}