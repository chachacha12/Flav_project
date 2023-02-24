package com.FLAVOR.mvp.feeds


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.FLAVOR.mvp.Adapter.GalleryAdapter
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.classes.Usersingleton
import com.FLAVOR.mvp.databinding.*
import com.FLAVOR.mvp.retrofit_service
import java.util.*

// 피드 게시물들 띄우는 리사이클러뷰의 어댑터
class HomeAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<Contents>,
    var server:retrofit_service,
    var onPostListener: OnPostdeleteListener,
    var binding2: ViewPostBinding

)  : RecyclerView.Adapter<HomeAdapter.MainViewHolder>() {

    //private lateinit var viewpostbinding: ViewPostBinding

    //전역
    private var MORE_INDEX = 2
    //firebaseHelper에서 activity값과 server값을 사용할거라 인자로 보내줌

    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

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

        val mainViewHolder = MainViewHolder(ItemPostBinding.bind(cardView))  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        //특정 게시글을 눌렀을때 효과
        cardView.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)
            intent.putExtra("postInfo", myDataset[mainViewHolder.adapterPosition])

            //contents객체의 멤버변수중에 jsonobject타입인 녀석들은 string타입으로 바꿔줘야만 intent로 보낼수있음
            intent.putExtra("user", myDataset[mainViewHolder.adapterPosition].User.toString())
            intent.putExtra("tag1", myDataset[mainViewHolder.adapterPosition].Tag_FirstAdj.toString())
            intent.putExtra("tag2", myDataset[mainViewHolder.adapterPosition].Tag_SecondAdj.toString())
            intent.putExtra("tag3", myDataset[mainViewHolder.adapterPosition].Tag_Location.toString())
            intent.putExtra("comments", myDataset[mainViewHolder.adapterPosition].Comments.toString())

            activity.startActivity(intent)
        }

        //게시글의 toolbar(점3개)버튼을 클릭했을때 효과
        mainViewHolder.binding.threePointButton.setOnClickListener {
            showPopup(it, mainViewHolder.adapterPosition)      //post.xml을 띄워줌. 밑에 있음. 구글에 android menu검색하고 developers사이트들어가서 코드 가져옴
        }                                                     //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.
        return mainViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        val safePosition: Int = holder.adapterPosition

        //val cardView = holder.cardView
        val titletextView = holder.binding.titleTextView
        val contents= myDataset[safePosition]
        titletextView.text = contents.restname  //컨텐츠의 식당명값을 제목에 넣어줌

        //내 게시물일때랑 친구게시물일때 나눠서 각각 다른 이미지 넣어줄거임
        if(contents.User.getString("kakao_id") == Usersingleton.kakao_id!!){  //내 게시물
            holder.binding.threePointButton.setBackgroundResource(R.drawable.ic_more_vert2)
        }else{
            holder.binding.threePointButton.setBackgroundResource(R.drawable.meeting)
        }

        //받아온 유저이름, 프로필 넣어주기
        var nameTextView =   holder.binding.nameTextView
        nameTextView.text = contents.User.getString("username")  //contents.User로 서버로부터 받아온 값이jsonobject라서 getString()~ 이걸 더 추가함

        var profile_photo_imageView =   holder.binding.photoImageVIew
        var photoUrl = contents.User.getString("profileimg_path")  //프사없으면"null"이라서 이때처리해주기

       if(photoUrl == "null"){  //프사없을땐 기본이미지로
           profile_photo_imageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
       }else{
           Glide.with(activity).load(photoUrl).override(500).thumbnail(0.1f)
               .into(profile_photo_imageView)
       }

        //받아온 지하철역, 거리정보 넣어주기
        val location_textView =   holder.binding.locationTextView
        location_textView.text = contents.near_station + "에서 "+contents.station_distance



        //게시물 하단의 태그3개 생성일을 채워줄 로직 - readContentsView는 view_post안의 뷰들을 채워줌
        val readContentsVIew: ReadContentsVIew =   holder.binding.readContentsView
        var contentsLayout = binding2.contentsLayout  //여기안에 contentsList의 내용들(사진 ) 등을 넣을거임
        //이미지, 동영상, 글 등 contents내용들을 담는 뷰들(이미지뷰, 텍스트뷰)만들고 데이터들 그 안에 넣을거임
        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(contents)) {     //데이터가 같을수도 있는데 계속 뷰들 다 지웠다 만들고 하는건 낭비라서 이 로직 추가함.(null일땐 처음 앱 실행할때를 위해) 이 로직 없다면 스크롤 내릴때마다 뷰들 삭제되고 생성되고했을거임
            contentsLayout.setTag(contents)
            contentsLayout.removeAllViews()   //액티비티 onResume()의 notifyDataSetChanged()를 통해 게시글 업데이트 해줄때마다 뷰 다 지우고 새롭게 만들어줄거임

            readContentsVIew.setMoreIndex(MORE_INDEX)
            readContentsVIew.setContents(contents, true)
        }
    }

    override fun getItemCount() = myDataset.size

    //피드상에서 바로 점세게버튼 중 하나 눌렀을때 동작
   //res안에 menu디렉토리 만든거에서, 그 안의 menu파일을 불러와서 보여주고, 클릭했을때 이벤트처리해줌
    private fun showPopup(v: View, position: Int) {
        val popup = PopupMenu(activity, v)

        if(myDataset[position].User.getString("kakao_id") == Usersingleton.kakao_id!!){
            //사용자가 선택한 게시물의 카카오id랑 내 카카오id랑 같을경우 (둘다 카카오에서 받아온 값)
            popup.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.post -> {
                        var builder = AlertDialog.Builder(activity)
                        builder.setMessage("게시물을 삭제할까요?")
                        builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지

                        builder.setPositiveButton(
                            "예"
                        ) { dialog, which ->
                            //게시물 삭제로직
                            onPostListener.onDelete(position)  //인터페이스를 통해 홈프래그먼트에서 삭제로직 작동시킬거임
                        }
                        builder.setNegativeButton(
                            "아니요"
                        ) { dialog, which -> }

                        builder.setNeutralButton(
                            "취소"
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
        }else{
            //다를경우 밥약신청버튼 보여줌
            popup.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.appointment -> {
                        var builder = AlertDialog.Builder(activity)
                        builder.setMessage(myDataset[position].User.getString("username")+"님에게 <" +
                                myDataset[position].restname+">에 같이 가자고 밥약속을 신청할까요?")
                        builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지
                        builder.setPositiveButton(
                            "예"
                        ) { dialog, which ->
                            //밥약속신청로직
                            onPostListener.onAppointment(position)  //인터페이스를 통해 홈프래그먼트에서
                        }
                        builder.setNegativeButton(
                            "아니요"
                        ) { dialog, which -> }

                        builder.setNeutralButton(
                            "취소"
                        ) { dialog, which -> }

                        builder.show() // 다이얼로그 보이기
                        true
                    }
                    else -> false
                }
            }
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.appointment, popup.menu)
            popup.show()
        }
    }  //showPopup




}