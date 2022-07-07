package com.FLAVOR.mvp.feeds


//포스트액티비티와 ComentsAdapter끼리 통신때 쓰는 인터페이스임. 댓글삭제돕고, 신고도움
// ComentsAdapter가 아닌 포스트액티비티에서 댓글리스트 업데이트 가능하게 해줌

interface OnCommentListener {
    fun onDelete(position: Int)  //댓글 삭제때씀
    fun onReport(position: Int)  //신고때씀
}