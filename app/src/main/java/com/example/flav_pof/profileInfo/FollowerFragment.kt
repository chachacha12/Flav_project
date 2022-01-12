package com.example.flav_pof.profileInfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.flav_pof.R
import com.example.flav_pof.feeds.Contents
import com.example.flav_pof.feeds.HomeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * A simple [Fragment] subclass.
 */
class FollowerFragment : Fragment() {

    private var followerAdapter: FollowerAdapter? = null
    private var topScrolled = false

    //플레브 서버로부터 컨텐츠 받아오기 위한 변수들
    private var contentsList: java.util.ArrayList<Contents>? = null

    //피드의 컨텐츠들 업데이트될동안 이곳에 새로운 컨텐츠 데이터들 받고 작업 다 끝나면 기존 contentsList에 다시 addall()해줄거임
    private var update_contentsList: java.util.ArrayList<Contents>? = null
    lateinit var recyclerView: RecyclerView  //서버로부터 컨텐츠들 다 가져오는 로직 끝난후에 handler함수에서 만들어줄거임
    //lateinit var loaderLayout: RelativeLayout  //전역으로둬야 모든 함수에서 쓸수있어서

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_follower, container, false)

        //로딩화면뷰 초기화
        //loaderLayout = view.findViewById<RelativeLayout>(R.id.loaderLayout)
       // loaderLayout.visibility = View.VISIBLE  //로딩화면

        contentsList = ArrayList()  //초기화  - 이거안하면 null에러남
        update_contentsList = ArrayList()  //초기화

        //followerAdapter = FollowerAdapter(requireActivity(), contentsList!!,server)  //어댑터에서도 server통신위해 server를 인자에 넣어줌

        recyclerView = view.findViewById<RecyclerView>(R.id.follower_recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = followerAdapter

        //loaderLayout.visibility = View.GONE  //로딩화면
        return view
    }


}
