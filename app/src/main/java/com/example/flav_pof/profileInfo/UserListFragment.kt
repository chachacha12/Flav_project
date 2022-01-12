package com.example.flav_pof.profileInfo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.databinding.FragmentUserListBinding
import com.example.flav_pof.retrofit_service
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.sdk.talk.TalkApiClient
import com.sothree.slidinguppanel.SlidingUpPanelLayout


//내 프로필 정보와 함께, 팔로우 팔로하는  친구목록 보여주는, 메인액티비티에 부착되는 3번째 프래그먼트
class UserListFragment(var server: retrofit_service) : Fragment() {
    private val TAG = "userListFragemnt태그"
    //뷰바인딩을 함 - xml의 뷰들에 접근하기 위해서
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    lateinit var slidePanel:SlidingUpPanelLayout  //슬라이드업파넬레이아웃

    private var userListAdapter: UserListAdapter? = null
    private var userList: ArrayList<UserInfo>? = null

    //tabLayout에 붙을 텍스트들
    var textArray = arrayListOf("팔로워", "팔로잉")
    //이 프래그먼트에 붙힐 프래그먼트 2개를 만들어줌
    var follower_fragment: FollowerFragment? =null
    var following_fragment: FollowingFragment? = null


    // 슬라이드업파넬레이아웃 이벤트 리스너
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        // 패널이 슬라이드 중일 때
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            // binding.tvSlideOffset.text = slideOffset.toString()
            Log.e("태그", "패널 슬라이드")
        }
        // 패널의 상태가 변했을 때
        override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                Log.e("태그", "열기")
                // binding.btnToggle.text = "열기"
            } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                //binding.btnToggle.text = "닫기"
                Log.e("태그", "닫기")
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserListBinding.inflate(inflater, container,false)
        val view = binding.root

        setprofileInfo()  //사용자 본인의 프로필정보를 singleton객체에서 가져와 삽입해줌

        init_viewpager()  //팔로잉, 팔로워 정보를 서버로부터 가져와서 띄워줌

        slidePanel = binding?.SlideUpPannerLayout!!   //fragment_map.xml의 가장 최상단 레이아웃을 가져옴
        slidePanel.addPanelSlideListener(PanelEventListener()) //슬라이드업파넬 이벤트 리스너 추가

        //친구찾기 버튼 눌렀을때
        binding.friendsLookButton.setOnClickListener {
            //리사이클러뷰 만들고 카톡친구가져오기 로직 진행.
            userList = ArrayList()
            userListAdapter = UserListAdapter(requireActivity(), userList!!, server)
            val recyclerView = binding.recyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = userListAdapter
            thread_start()
            //패널 열고 닫기
            val state = slidePanel.panelState
            // 닫힌 상태일 경우 열기
            if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
        }
        //슬라이드 열린거 내려주는 버튼
        binding.pulldownButton.setOnClickListener {
            val state = slidePanel.panelState
            // 열린 상태일 경우 닫기
            if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }
        return view
    }  //onCreateView

    //팔로잉, 팔로워 정보를 서버로부터 가져와서 띄워줌
    fun init_viewpager(){
        //프래그먼트들 여기서 초기화
        follower_fragment = FollowerFragment()
        following_fragment = FollowingFragment()

        /*
        //프래그먼트로 식당명 데이터 전달
        if(namelist_string == "") {  //게시물 수정하기 눌러서 온 경우엔 null임

        }else{
            var bundle = Bundle()
            bundle.putString("namelist_string", namelist_string)  //식당명, 위경도 묶음jsonarray가  string묶음으로 된걸 frag에 보내줌
            name_fragment!!.arguments = bundle
        }

         */

        //뷰페이저에 다시 프래그먼트들을 붙혀줌. 이때 어댑터에 인자를 하나 추가해서 내가 위에서 bundle넣어서 새로 만든 프래그먼트를 어댑터에 전달해줌
        binding.viewpager2.adapter = follower_following_Viewpager_Adapter(
            requireActivity(),
            follower_fragment,
            following_fragment
        )

        //뷰페이저2객체를 슬라이딩 할때마다 tab의 위치도 바뀌어야함. 그 둘을 동기화 해주는 클래스인 TabLayoutMediator을 이용해줌.
        TabLayoutMediator(binding.tabLayout, binding.viewpager2){ tab, position -> tab.text = textArray[position]
        }.attach()

       // tag_fragment?.gettag1(server)  //태그 프래그먼트 객체통해 서버로부터 태그1값 가져오기
        Log.e("태그", "뷰페이저만들어짐.")
    }


    //사용자 본인의 프로필 정보 세팅
    fun setprofileInfo(){
        binding.nameTextView.text = "  "+Usersingleton.username
        binding.emailTextView.text ="  "+Usersingleton.userEmail
        Glide.with(requireActivity()).load(Usersingleton.profilepath).override(500).thumbnail(0.1f)
            .into( binding.profileImageView)
        Log.e("태그","유저프로필 세팅 binding.nameTextView.text: "+Usersingleton.username)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }



    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    private fun postsUpdate() {
        // 카카오톡 친구 목록 가져오기 (기본)
        TalkApiClient.instance.friends { friends, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 친구 목록 가져오기 실패", error)
            } else if (friends != null) {
                Log.i(TAG, "카카오톡 친구 목록 가져오기 성공 \n${friends.elements?.joinToString("\n")}")
                // userList!!.clear()
                var i = 0
                repeat(friends.elements?.size!!) {
                    userList!!.add(
                        UserInfo(
                            friends.elements?.get(i)?.profileNickname!!,
                            friends.elements?.get(i)?.profileThumbnailImage!!,
                            friends.elements?.get(i)?.id.toString()
                        )
                    )
                    i++
                }
                Log.e(TAG, "userList:  " + userList.toString())
                handler()
                Log.e(TAG, "카톡 친구목록 가져오기 성공!  핸들러 실행하여 리사이클러뷰 notifiy ")
                // 친구의 UUID 로 메시지 보내기 가능
            }
        }
    }

    private fun thread_start() {
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("친구리스트 태그", "thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                postsUpdate()
                Log.e("친구리스트 태그", "getData성공. 데이터 가져옴")
            } catch (e: Exception) {
                Log.e("친구리스트 태그", "getData실패")
            }
        }
    }

    private fun handler() {
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                //데이터 가져오는 작업 다 끝나고 실행시킬 내용들
                userListAdapter!!.notifyDataSetChanged()  //리사이클러뷰에 바뀐 데이터값 다시 업데이트
            }
        }
        handler.obtainMessage().sendToTarget()
    }


}
