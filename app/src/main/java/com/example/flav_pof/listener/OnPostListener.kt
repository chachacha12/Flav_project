package com.example.flav_pof.listener

import com.example.flav_pof.PostInfo

interface OnPostListener {
   // fun onDelete(position: Int)    //선택된 게시물의 id값을 전달할거임
   // fun onModify(position: Int)

    fun onDelete(postInfo: PostInfo)
    fun onModify()

}