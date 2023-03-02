package com.FLAVOR.mvp.feeds

import android.content.Context
import android.os.Build
import android.provider.Settings.Secure.getString
import android.util.AttributeSet
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.databinding.ActivityPostBinding
import com.FLAVOR.mvp.databinding.ViewPostBinding
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_post.view.*
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
        layoutInflater.inflate(com.FLAVOR.mvp.R.layout.view_post, this, true)
    }

    fun setMoreIndex(moreIndex: Int) {
        this.moreIndex = moreIndex
    }

    //view_post의 컨텐츠값들 여기서 다 넣어줌
    @RequiresApi(Build.VERSION_CODES.O)
    fun setContents(contents: Contents, comment_num:Boolean) {  //홈어댑터에선 댓글갯수보여줘야해서 true. 포스트액티비티에선 false
        //받아온 게시물생성일값 넣어주기
        val createdAtTextView = findViewById<TextView>(com.FLAVOR.mvp.R.id.createAtTextView)
        val instant = Instant.parse(contents.date)  //contents.date가 string날짜값임.
        var date = Date.from(instant)   //기존 string날짜값을 date타입으로 만듬
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

        val cal = Calendar.getInstance()
        cal.time = date
        val createdAt: String = simpleDateFormat.format(cal.time)  // 원하는대로 포맷된 string날짜값임
        createdAtTextView.text = createdAt
        Log.e("태그", "포스태액티빗에 띄우기 위해 readcontentsView에서 만든 createdAt: " + createdAt)

        //받아온 태그값 넣어주기
        val tag1_textView = findViewById<TextView>(com.FLAVOR.mvp.R.id.tag1_textView)
        val tag2_textView = findViewById<TextView>(com.FLAVOR.mvp.R.id.tag2_textView)
        val tag3_textView = findViewById<TextView>(com.FLAVOR.mvp.R.id.tag3_textView)
        val comment_num_textView = findViewById<TextView>(com.FLAVOR.mvp.R.id.textView10)

        tag1_textView.text = contents.Tag_FirstAdj.getString("tagname")
        //jsonobject타입으로 result라는 jsonArray안에 담아져서 오는 인자는 이렇게 처리
        tag2_textView.text = contents.Tag_SecondAdj.getString("tagname")
        tag3_textView.text = contents.Tag_Location.getString("tagname")


        if(comment_num){
            comment_num_constraint.visibility = View.VISIBLE
           // viewpostbinding.commentNumConstraint.visibility = View.VISIBLE
            //댓글몇개인지 보여주기
            val comment_num = contents.Comments.length()
            comment_num_textView.text = comment_num.toString()
        }else{
            comment_num_constraint.visibility = View.GONE
            //viewpostbinding.commentNumConstraint.visibility = View.GONE
        }

        //이미지넣어줄 부모뷰 세팅
        val contentsLayout = findViewById<LinearLayout>(com.FLAVOR.mvp.R.id.contentsLayout)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )        //생성되는 음식사진 이미지뷰의 길이는 여기서 정해줌
 
        //받아온 이미지 넣어주기
        val photoUrl = contents.filepath  //컨텐츠에 있는 사진경로값
        if (Patterns.WEB_URL.matcher(photoUrl)
                .matches() && photoUrl.contains(resources.getString(R.string.release_s3_ContainsUrl))) {  //배포용 url값:  release_s3_ContainsUrl /  개발용 url값: debug_s3_ContainsUrl
            val imageView = ImageView(context)
            imageView.layoutParams = layoutParams
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            contentsLayout.addView(imageView)
            Glide.with(this).load(photoUrl).override(1000).thumbnail(0.1f)
                .into(imageView)
        }
    }


}