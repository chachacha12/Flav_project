package com.FLAVOR.mvp.classes

import com.google.gson.annotations.SerializedName

//식당리스트 반환받을때만 쓰는 클래스
//Flav에서 반환받은 json값이  Name객체로 반환
data class Name(@SerializedName("length") var length: Int,  @SerializedName("result") var result: List<Any>) {

}