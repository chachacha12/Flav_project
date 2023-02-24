package com.FLAVOR.mvp.feeds


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import com.FLAVOR.mvp.Adapter.GalleryAdapter
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.databinding.ActivityGalleryBinding
import com.FLAVOR.mvp.databinding.ActivityPostBinding
import com.FLAVOR.mvp.databinding.ItemComentsBinding
import com.FLAVOR.mvp.databinding.ItemGalleryBinding
import com.FLAVOR.mvp.databinding.ItemPostBinding
import com.FLAVOR.mvp.retrofit_service
import com.bumptech.glide.Glide
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response.error
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

//포스트액티비티에서 사용
// 댓글띄우는 리사이클러뷰의 어댑터
class ComentsAdapter(
    var activity: Activity,
    private var myDataset:ArrayList<JSONObject>,
    var server:retrofit_service,
    var onCommentListener: OnCommentListener

)  : RecyclerView.Adapter<ComentsAdapter.CommentViewHolder>() {

    private lateinit var binding: ItemComentsBinding


    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class CommentViewHolder(val binding: ItemComentsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(    //레이아웃 item_coments에 있는 카드뷰를 가리키는 뷰홀더를 만듬. 이건 처음에 액티비티에서 recyclerView.adapter = mainAdapter 할때만 작동하고 그후엔 안함.
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {

        val cardView: CardView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_coments,
            parent,
            false
        ) as CardView   //item_coments 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.


        val commentviewHolder =
            CommentViewHolder(ItemComentsBinding.bind(cardView))  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성



        //댓글의 toolbar(점3개)버튼을 클릭했을때 효과
        commentviewHolder.binding.commentDeleteButton.setOnClickListener {

            /*
            val builder = AlertDialog.Builder(activity)
            builder.setMessage("삭제하시겠습니까?")
            builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지
            builder.setPositiveButton(
                "예"
            ) { dialog, which ->
                //게시물 삭제로직
                onCommentListener.onDelete(mainViewHolder.adapterPosition)  //인터페이스를 통해  PostActivity에서 삭제로직 작동시킬거임
            }
            builder.setNegativeButton(
                "아니요"
            ) { dialog, which -> }
            builder.show()

             */

            showPopup(  //원래 삭제버튼아닌, 점세개버튼이었을때 동작로직
                it,
                commentviewHolder.adapterPosition
            )      //post.xml을 띄워줌. 밑에 있음. 구글에 android menu검색하고 developers사이트들어가서 코드 가져옴

        }                                                     //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.
        return commentviewHolder
    }

    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
    //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val safePosition: Int = holder.adapterPosition
        val commmentsCardView = holder.binding.cardView


        var photoimageView = holder.binding.photoImageVIew
        var usernameTextView = holder.binding.nameTextView
        val commentTextView = holder.binding.comentsTextView
        val dateTextView = holder.binding.dateTextView
        val delete_button = holder.binding.commentDeleteButton

        //댓글내용
        val contents = myDataset[position].getString("content")  //리스트에서 순서대로 댓글의 내용들을 하나씩 가져옴
        commentTextView.text = contents.toString()  //텍스트뷰에 댓글 뛰움

        //작성일    (String날짜값 -> date타입변환 -> 원하는 형식 포맷 -> String타입으로 변환)
        val date_string = myDataset[position].getString("createdAt")  //작성일을 가져옴
        val instant = Instant.parse(date_string)  //date_string가 string날짜값임.
        val date = Date.from(instant)   //기존 string날짜값을 date타입으로 만듬
        val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm")   //yyyy-MM-dd HH:mm
        val cal = Calendar.getInstance()
        cal.time = date
        val createdAt: String = simpleDateFormat.format(cal.time)  // 원하는대로 포맷된 string날짜값임
        dateTextView.text = createdAt  //작성일텍스트뷰안에 포맷된 String날짜값을 넣음

        //유저이름
        val username = myDataset[position].getString("username")    //댓글 쓴 사용자 이름 가져옴
        usernameTextView.text = username

        //유저프사
        val userprofile = myDataset[position].getString("profileimg_path")    //프사가져옴
        if (userprofile == "null") {  //프사없을땐 기본이미지로
            photoimageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
        } else {
            Glide.with(activity).load(userprofile).override(200).thumbnail(0.1f)
                .into(photoimageView)
        }

        /*
          //내가 쓴 댓글에만 삭제버튼 띄우기
        if (myDataset[position].getString("kakao_id") == Usersingleton.kakao_id!!) {
            delete_button.visibility = View.VISIBLE
        }else{
            delete_button.visibility = View.GONE
        }
         */
    }

    //삭제버튼 눌렀을때 동작
    //res안에 menu디렉토리 만든거에서, 그 안의 menu파일을 불러와서 보여주고, 클릭했을때 이벤트처리해줌
    @SuppressLint("LongLogTag")
    private fun showPopup(v: View, position: Int) {
        val popup = PopupMenu(activity, v)
        try {
            if (myDataset[position].getString("kakao_id") == Usersingleton.kakao_id!!) {
                //사용자가 선택한 댓글의 카카오id랑 내 카카오id랑 같을경우: 삭제가능
                popup.setOnMenuItemClickListener {
                    return@setOnMenuItemClickListener when (it.itemId) {
                        R.id.post -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setMessage("삭제하시겠습니까?")
                            builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지
                            builder.setPositiveButton(
                                "예"
                            ) { dialog, which ->
                                //게시물 삭제로직
                                onCommentListener.onDelete(position)  //인터페이스를 통해  PostActivity에서 삭제로직 작동시킬거임
                            }
                            builder.setNegativeButton(
                                "아니요"
                            ) { dialog, which -> }
                            builder.show() // 다이얼로그 보이기
                            true
                        }
                        else -> false
                    }
                }
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.post, popup.menu)
                popup.show()
            } else {
                //다를경우: 신고가능
                popup.setOnMenuItemClickListener {
                    return@setOnMenuItemClickListener when (it.itemId) {
                        R.id.report -> {
                            val builder = AlertDialog.Builder(activity)
                            builder.setMessage("신고하시겠습니까?")
                            builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지
                            builder.setPositiveButton(
                                "예"
                            ) { dialog, which ->
                                //신고로직
                                onCommentListener.onReport(position)
                            }
                            builder.setNegativeButton(
                                "아니요"
                            ) { dialog, which -> }
                            builder.show() // 다이얼로그 보이기
                            true
                        }
                        else -> false
                    }
                }
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.report, popup.menu)
                popup.show()
            }
        } catch (e: JSONException) {
            Log.e("태그:", "JSONException: " + e.toString())
        }
    }


    override fun getItemCount() =
        myDataset.size
}