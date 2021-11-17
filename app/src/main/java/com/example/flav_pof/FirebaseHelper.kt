package com.example.flav_pof

import android.app.Activity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.flav_pof.listener.OnPostListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

//스토리지, 스토어에서 게시물 삭제시켜주는 로직을 책임지는 클래스
class FirebaseHelper(private val activity: Activity) {
    private var onPostListener: OnPostListener? = null
    private var successCount = 0


    fun setOnPostListener(onPostListener: OnPostListener?) {
        this.onPostListener = onPostListener
    }


    fun storageDelete(postInfo: PostInfo) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val id = postInfo.id
        val contentsList = postInfo.contents
        for (i in contentsList.indices) {
            val contents = contentsList[i]

            if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts")) {


                successCount++
                val desertRef = storageRef.child("posts/" + id + "/" + contents.split("\\?")[0].split("%2F")[contents.split("\\?")[0].split("%2F").size - 1])
                desertRef.delete().addOnSuccessListener {
                    successCount--
                    storeDelete(id)
                }.addOnFailureListener {
                    Log.e("태그","contents: "+ contents)
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        storeDelete(id)
    }

    private fun storeDelete(id: String?) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        if (successCount == 0) {
            firebaseFirestore.collection("posts").document(id!!)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(activity, "게시글을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                    onPostListener!!.onDelete()
                    //postsUpdate();
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "게시글을 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}