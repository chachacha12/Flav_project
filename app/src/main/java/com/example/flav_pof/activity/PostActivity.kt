package com.example.flav_pof.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import java.text.SimpleDateFormat
import java.util.*


class PostActivity : BasicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        var postInfo: PostInfo = intent.getSerializableExtra("postInfo") as PostInfo  //형변환 꼭 해주기
        var titletextView2: TextView =
            findViewById(R.id.titleTextView) //이부분 오류뜰수도
        titletextView2.text = postInfo.title


        val createdAtTextView = findViewById<TextView>(R.id.createAtTextView)
        createdAtTextView.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                postInfo.createdAt
            )
        )

        val contentsLayout = findViewById<LinearLayout>(R.id.contentsLayout)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        var contentsList = postInfo.contents


        if (contentsLayout.tag == null || contentsLayout.tag != contentsList) {
            contentsLayout.tag = contentsList
            contentsLayout.removeAllViews()
            for (i in 0 until contentsList.size) {
                val contents = contentsList[i]
                if (Patterns.WEB_URL.matcher(contents)
                        .matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts")
                ) {
                    val imageView = ImageView(this)
                    imageView.setLayoutParams(layoutParams)
                    imageView.setAdjustViewBounds(true)
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY)
                    contentsLayout.addView(imageView)
                    Glide.with(this).load(contents).override(1000).thumbnail(0.1f).into(imageView)
                } else {
                    val textView = TextView(this)
                    textView.layoutParams = layoutParams
                    textView.text = contents
                    textView.setTextColor(Color.rgb(0, 0, 0))
                    contentsLayout.addView(textView)
                }
            }
        }


    }
}
