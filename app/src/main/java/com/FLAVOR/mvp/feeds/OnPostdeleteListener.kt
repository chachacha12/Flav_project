package com.FLAVOR.mvp.feeds


//홈프래그먼트와 홈어댑터끼리 통신때 쓰는 인터페이스임. 게시물을 삭제돕고,
// 홈어댑터가 아닌 홈프래그먼트에서 피드 업데이트 가능하게 해줌

interface OnPostdeleteListener {
    fun onDelete(position: Int)  //게시물 삭제때씀
    fun onAppointment(position: Int)  //밥약속 신청때씀
}