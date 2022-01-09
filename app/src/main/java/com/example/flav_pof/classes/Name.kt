package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

//식당리스트 반환받을때만 쓰는 클래스
//Flav에서 반환받은 json값이  Name객체로 반환
data class Name(@SerializedName("default_lat") var default_lat: String, @SerializedName("default_lng") var default_lng: String, @SerializedName("result") var result: List<Any>) {

}