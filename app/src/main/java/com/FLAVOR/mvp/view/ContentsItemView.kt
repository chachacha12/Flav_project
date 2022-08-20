package com.FLAVOR.mvp.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Nullable
import com.bumptech.glide.Glide

//게시물 작성창에서 갤러리에서 선택한 이미지를 담아서 보여줄 객체
class ContentsItemView : LinearLayout {
    private var imageView: ImageView? = null
    //private var editText: EditText? = null
    //private var text_LinearLayout: LinearLayout? = null

    constructor(context: Context?) : super(context) {
        initView()
    }

    constructor(context: Context?, @Nullable attributeSet: AttributeSet?) : super(
        context,
        attributeSet
    ) {
        initView()
    }

    private fun initView() {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orientation = VERTICAL
        //view_contents_image.xml에 있는 이미지뷰에서 게시물작성창의 이미지 크기 스케일을 정함
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        addView(layoutInflater.inflate(com.FLAVOR.mvp.R.layout.view_contents_image , this, false))
        //addView(layoutInflater.inflate(com.example.flav_pof.R.layout.view_contents_edit_text, this, false))
        //addView(layoutInflater.inflate(com.example.flav_pof.R.layout.view_contents_text_linearlayout, this, false))

        imageView = findViewById(com.FLAVOR.mvp.R.id.contentsImageView)
        //editText = findViewById(com.example.flav_pof.R.id.contentsEditText)
        //text_LinearLayout = findViewById(com.example.flav_pof.R.id.contentsTextLinearLayout)
    }

    fun setImage(path: String?) {
        Glide.with(this).load(path).override(1000).into(imageView!!)
    }

    /*
    fun setText(text: String?) {
        editText!!.setText(text)
    }

     */

    /*
    //식당명 텍스트를 받아서 텍스트뷰 생성
    fun addtextView(textView:TextView?){
        text_LinearLayout!!.addView(textView)
    }
     */

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        imageView!!.setOnClickListener(onClickListener)
    }

    /*
    override fun setOnFocusChangeListener(onFocusChangeListener: OnFocusChangeListener) {
        editText!!.onFocusChangeListener = onFocusChangeListener
    }

     */
}
