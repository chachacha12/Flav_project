package com.example.flav_pof.profileInfo

import com.google.firebase.auth.UserInfo



//팔로잉 프래그먼트와 팔로잉어댑터끼리 통신때도 씀. 팔로잉 유저삭제 후 업데이트
interface OnfollowingdeleteListener {
    fun onDelete(userInfo: com.example.flav_pof.profileInfo.UserInfo)

}