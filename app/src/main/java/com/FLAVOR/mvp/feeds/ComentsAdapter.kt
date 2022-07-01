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
import org.json.JSONArray
import java.util.*

// 댓글띄우는 리사이클러뷰의 어댑터
class ComentsAdapter(
    var activity: Activity,
    private var myDataset:Contents,
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


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.e("태그","피드 만들어주는 홈프래그먼트의 onbindView 시작")
        val safePosition: Int = holder.adapterPosition

        val commmentsCardView = holder.cardView
        /*
        var photoimageView = commmentsCardView.photoImageVIew
        var usernameTextView = commmentsCardView.nameTextView
        var dateTextView = commmentsCardView.date_textView
         */
        val commentTextView = commmentsCardView.coments_TextView
        val comment_Jsonarray= myDataset.Comments   //댓글id , 카카오id, 내용 들어있는 jsonArray임
        val comment_Object = comment_Jsonarray.getJSONObject(position)  //하나하나의 댓글 JsonObject를 가져옴
        val contents = comment_Object.getString("content")  //댓글의 내용들을 하나씩 가져옴
        commentTextView.text = contents.toString()  //텍스트뷰에 댓글 뛰움

        //val kakaoid = comment_Object.getString("kakao_id")  //댓글 쓴 사용자의 카카오id를 가져옴
        //kakaoid얻은걸로 api호출해서 해당 유저의 프사, 이름 가져올거임....

    }


    override fun getItemCount() =
        myDataset.Comments.length()



}