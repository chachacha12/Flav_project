package com.example.flav_pof.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.example.flav_pof.PostInfo
import java.text.SimpleDateFormat
import java.util.*


class ReadContentsVIew : LinearLayout {
    private var mycontext: Context? = null   //그냥 context라고 변수명을 지으면 메소드명(getcontext() )과 겹쳐서 에러나므로 mycontext라고 지어줌
    private var moreIndex = -1

    constructor(context: Context) : super(context) {
        this.mycontext = context
        initView()
    }

    constructor(context: Context, @Nullable attributeSet: AttributeSet?) : super(
        context,
        attributeSet
    ) {
        this.mycontext = context
        initView()
    }

    private fun initView() {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orientation = VERTICAL
        val layoutInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(com.example.flav_pof.R.layout.view_post, this, true)
    }

    fun setMoreIndex(moreIndex: Int) {
        this.moreIndex = moreIndex
    }

    fun setPostInfo(postInfo: PostInfo) {
        val createdAtTextView = findViewById<TextView>(com.example.flav_pof.R.id.createAtTextView)
        createdAtTextView.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                postInfo.createdAt
            )
        )
        val contentsLayout = findViewById<LinearLayout>(com.example.flav_pof.R.id.contentsLayout)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val contentsList = postInfo.contents
        for (i in 0 until contentsList.size) {
            if (i == moreIndex) {
                val textView = TextView(context)
                textView.layoutParams = layoutParams
                textView.text = "더보기..."
                contentsLayout.addView(textView)
                break
            }
            val contents = contentsList[i]
            if (Patterns.WEB_URL.matcher(contents)
                    .matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts")) {
                val imageView = ImageView(context)
                imageView.layoutParams = layoutParams
                imageView.adjustViewBounds = true
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                contentsLayout.addView(imageView)
                Glide.with(this).load(contents).override(1000).thumbnail(0.1f)
                    .into(imageView)
            } else {
                val textView = TextView(context)
                textView.layoutParams = layoutParams
                textView.text = contents
                textView.setTextColor(Color.rgb(0, 0, 0))
                contentsLayout.addView(textView)
            }
        }
    }
}