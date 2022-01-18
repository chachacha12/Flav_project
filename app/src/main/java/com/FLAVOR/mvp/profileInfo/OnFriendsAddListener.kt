package com.FLAVOR.mvp.profileInfo

//uerlist 프래그먼트와 uerlist어댑터끼리 통신때도 씀. 팔로잉 유저삭제 후 업데이트
interface OnFriendsAddListener {
    fun onAdd(followed_id:String)  //내가 선택한 특정 유저의 카카오id값을 가져올거임 (내가 친추하고자하는 친구)

}