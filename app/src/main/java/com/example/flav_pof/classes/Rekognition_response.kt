package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName

//Flav에서 반환받은 json값이  Name객체로 반환
data class Rekognition_response(@SerializedName("rekogData") var rekogData: result) {
}

data class result(var Labels: List<Any>, var LabelModelVersion: String, var isfood: Boolean){}