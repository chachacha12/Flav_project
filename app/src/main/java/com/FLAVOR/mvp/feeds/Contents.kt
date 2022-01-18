package com.FLAVOR.mvp.feeds

import org.json.JSONObject
import java.io.Serializable

data class Contents(
    val contents_id: Int,
    val date: String,
    var filename: String,
    var filepath: String,
    val restname: String,

    val user_id: Int,
    val adj1_id: Int,
    val adj2_id: Int,
    val locationtag_id: Int,

    val lat: String,
    val lng: String,
    val near_station: String,
    val station_distance: String,

    @Transient
    var User:JSONObject,
    @Transient
    var Tag_FirstAdj:JSONObject,
    @Transient
    var Tag_SecondAdj:JSONObject,
    @Transient
    var Tag_Location:JSONObject

) : Serializable {


}