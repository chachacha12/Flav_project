package com.example.flav_pof.Adapter

//GalleryAdapter클래스를 복사해서 좀 바꿔서 써준 어댑터임

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.FirebaseHelper
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.activity.PostActivity
import com.example.flav_pof.writepost.WritePostActivity
import com.example.flav_pof.listener.OnPostListener
import com.example.flav_pof.view.ReadContentsVIew
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.view_post.view.*
import java.util.*


//괄호안은 어댑터클래스의 인자들
class HomeAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<PostInfo>
)  : RecyclerView.Adapter<HomeAdapter.MainViewHolder>() {

    //전역
    private var MORE_INDEX = 2
    private var firebaseHelper = FirebaseHelper(activity)  //firebaseHelper 객체생성


    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(    //레이아웃 item_post에 있는 카드뷰를 가리키는 뷰홀더를 만듬. 이건 처음에 액티비티에서 recyclerView.adapter = mainAdapter 할때만 작동하고 그후엔 안함.
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_post,
            parent,
            false
        ) as CardView   //item_post에 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.

        val mainViewHolder = MainViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        //특정 게시글을 눌렀을때 효과
        cardView.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)

            intent.putExtra("postInfo", myDataset[mainViewHolder.adapterPosition])
            activity.startActivity(intent)
        }


        //게시글의 toolbar(점3개)버튼을 클릭했을때 효과
        cardView.menu.setOnClickListener {
            showPopup(it, mainViewHolder.adapterPosition)      //post.xml을 띄워줌. 밑에 있음. 구글에 android menu검색하고 developers사이트들어가서 코드 가져옴
        }                                                     //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.

        return mainViewHolder
    }

    fun setOnPostListener(onPostListener: OnPostListener){
        firebaseHelper.setOnPostListener(onPostListener)
    }



    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        val safePosition: Int = holder.adapterPosition

        var cardView = holder.cardView
        var titletextView = cardView.titleTextView
        var postInfo= myDataset[safePosition]
        titletextView.text = postInfo.title

        val readContentsVIew: ReadContentsVIew = cardView.findViewById(R.id.readContentsView)

        var contentsLayout = cardView.contentsLayout  //여기안에 contentsList의 내용들(사진,영상,글) 등을 넣을거임


        //이미지, 동영상, 글 등 contents내용들을 담는 뷰들(이미지뷰, 텍스트뷰)만들고 데이터들 그 안에 넣을거임
        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {     //데이터가 같을수도 있는데 계속 뷰들 다 지웠다 만들고 하는건 낭비라서 이 로직 추가함.(null일땐 처음 앱 실행할때를 위해) 이 로직 없다면 스크롤 내릴때마다 뷰들 삭제되고 생성되고했을거임
            contentsLayout.setTag(postInfo)
            contentsLayout.removeAllViews()   //액티비티 onResume()의 notifyDataSetChanged()를 통해 게시글 업데이트 해줄때마다 뷰 다 지우고 새롭게 만들어줄거임

            readContentsVIew.setMoreIndex(MORE_INDEX)
            readContentsVIew.setPostInfo(postInfo)
        }
    }


    override fun getItemCount() = myDataset!!.size


   //res안에 menu디렉토리 만든거에서, 그 안의 menu파일을 불러와서 toolbar보여주고, 클릭했을때 이벤트처리해줌  //developers사이트에서 가져온 함수.
    private fun showPopup(v: View, position: Int) {
        val popup = PopupMenu(activity, v)
        popup.setOnMenuItemClickListener {

            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.modify -> {                    //수정하기 눌렀을때
                    myStartActivity(WritePostActivity::class.java, myDataset[position])
                    true
                }
                R.id.delete -> {                  //삭제하기 눌렀을때
                    firebaseHelper.storageDelete(myDataset[position])
                    true
                }
                else -> false
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.post, popup.menu)
        popup.show()
    }


    private fun myStartActivity(c: Class<*>, postInfo: PostInfo) {
        val intent = Intent(activity, c)
        intent.putExtra("postInfo", postInfo)
        activity.startActivity(intent)
    }


}