package com.example.flav_pof

import com.google.gson.annotations.SerializedName

//Flav에서 반환받은 json값이  Name객체로 반환
data class Name( @SerializedName("name") var name: List<String>) {

}