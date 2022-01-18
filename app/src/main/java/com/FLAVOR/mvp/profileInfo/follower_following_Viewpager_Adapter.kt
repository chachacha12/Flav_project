package com.FLAVOR.mvp.profileInfo

//viewpager2.adapter에 붙힐 어댑터임.

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

//두번째 인자인 프래그먼트는 내가 추가해준것임. 사용자가 스피너에서 특정 이용자 선택했을때,
//그때 액티비티에서 bundle객체 넣고 만든 프래그먼트를 여기서 받아와서 그걸로 프래그먼트 만들어 줄거임.
class follower_following_Viewpager_Adapter(fragmentActivity: FragmentActivity, fragment1: Fragment? = null, fragment2: Fragment? = null) : FragmentStateAdapter(fragmentActivity) {

    var gfrag = fragment1
    var afrag = fragment2

    override fun getItemCount(): Int {  //몇개의 아이템(프래그먼트?)들을 제공해 줄건지 정하기
        return 2
    }

    override fun createFragment(position: Int): Fragment {   //페이지마다 어떤 프래그먼트를 줄지 정하기
        return when(position){
            0-> {
                gfrag!!  //즉, 이 프래그먼트는 내가 액티비티에서 만들어준, 특정이용자의 id값 정보를  bundle에 가진 그래프 프래그먼트이다.
            }
            1-> afrag!!
            else -> gfrag!!
        }
    }





}

