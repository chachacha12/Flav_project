package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName

//컨텐츠 업로드api의 반환값 받는 클래스
data class ContentsUpload_response(@SerializedName("msg") var msg: String, @SerializedName("content_id") var content_id: Int) {

}