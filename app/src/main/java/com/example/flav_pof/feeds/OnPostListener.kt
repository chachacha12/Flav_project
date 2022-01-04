package com.example.flav_pof.feeds

import com.example.flav_pof.classes.ContentsUpload_request

interface OnPostListener {
   // fun onDelete(position: Int)    //선택된 게시물의 id값을 전달할거임
   // fun onModify(position: Int)

    //fun onDelete(postInfo: PostInfo)
    fun onDelete(contents: Contents)
    fun onModify()

}