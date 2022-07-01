package com.FLAVOR.mvp.classes

import com.google.gson.annotations.SerializedName

//댓글업로드 반환값 받는 클래스
data class CommentUpload_response(@SerializedName("msg") var msg: String, @SerializedName("comment_id") var comment_id: Int) {

}