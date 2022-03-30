package com.FLAVOR.mvp.feeds


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.retrofit_service
import kotlinx.android.synthetic.main.item_coments.view.*
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.item_post.view.nameTextView
import kotlinx.android.synthetic.main.item_post.view.photoImageVIew
import kotlinx.android.synthetic.main.view_post.view.*
import java.util.*

// 댓글띄우는 리사이클러뷰의 어댑터
class ComentsAdapter(
    var activity: Activity,
    private var myDataset:String,
    var server:retrofit_service

)  : RecyclerView.Adapter<ComentsAdapter.MainViewHolder>() {


    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(    //레이아웃 item_coments에 있는 카드뷰를 가리키는 뷰홀더를 만듬. 이건 처음에 액티비티에서 recyclerView.adapter = mainAdapter 할때만 작동하고 그후엔 안함.
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_coments,
            parent,
            false
        ) as CardView   //item_coments 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.

        val mainViewHolder = MainViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        return mainViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.e("태그","피드 만들어주는 홈프래그먼트의 onbindView 시작")
        val safePosition: Int = holder.adapterPosition

        var commmentsCardView = holder.cardView
        /*
        var photoimageView = commmentsCardView.photoImageVIew
        var usernameTextView = commmentsCardView.nameTextView
        var dateTextView = commmentsCardView.date_textView
         */
        var commentTextView = commmentsCardView.coments_TextView


        var contents= myDataset[safePosition]


    }

    override fun getItemCount() = myDataset.length

}