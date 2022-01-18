package com.FLAVOR.mvp.classes

import com.google.gson.annotations.SerializedName

//Flav에서 반환받은 json값이  Name객체로 반환
data class Result_response(@SerializedName("result") var result: List<Any>) {

}