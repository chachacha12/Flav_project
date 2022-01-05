package com.example.flav_pof.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.flav_pof.feeds.Contents
import kotlinx.android.synthetic.main.item_post.view.*
import java.text.SimpleDateFormat
import java.time.Instant
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

    //view_post의 컨텐츠값들 여기서 다 넣어줌
    @RequiresApi(Build.VERSION_CODES.O)
    fun setContents(contents: Contents) {
        //받아온 게시물생성일값 넣어주기
        val createdAtTextView = findViewById<TextView>(com.example.flav_pof.R.id.createAtTextView)
        val instant = Instant.parse(contents.date)  //contents.date가 string날짜값임.
        var date = Date.from(instant)   //기존 string날짜값을 date타입으로 만듬
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val cal = Calendar.getInstance()
        cal.time = date
        val createdAt: String = simpleDateFormat.format(cal.time)  // 원하는대로 포맷된 string날짜값임
        createdAtTextView.text = createdAt

        //받아온 태그값 넣어주기
        val tag1_textView = findViewById<TextView>(com.example.flav_pof.R.id.tag1_textView)
        val tag2_textView = findViewById<TextView>(com.example.flav_pof.R.id.tag2_textView)
        val tag3_textView = findViewById<TextView>(com.example.flav_pof.R.id.tag3_textView)
        tag1_textView.text = "#"+contents.adj1_id.toString()
        tag2_textView.text = "#"+contents.adj2_id.toString()
        tag3_textView.text = "#"+contents.locationtag_id.toString()


        //이미지넣어줄 부모뷰 세팅
        val contentsLayout = findViewById<LinearLayout>(com.example.flav_pof.R.id.contentsLayout)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //받아온 이미지 넣어주기
        val photoUrl = contents.filepath  //컨텐츠에 있는 사진경로값
        if (Patterns.WEB_URL.matcher(photoUrl)
                .matches() && photoUrl.contains("https://flavbucket.s3.ap-northeast-2.amazonaws.com/")) {
            val imageView = ImageView(context)
            imageView.layoutParams = layoutParams
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            contentsLayout.addView(imageView)
            Glide.with(this).load(photoUrl).override(1000).thumbnail(0.1f)
                .into(imageView)
        }


    }











    /*
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

     */
}