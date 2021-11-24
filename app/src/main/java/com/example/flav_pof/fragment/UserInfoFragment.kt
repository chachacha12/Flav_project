package com.example.flav_pof.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.flav_pof.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 */
class UserInfoFragment : Fragment() {
    private val TAG = "UserInfoFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_user_info, container, false)
        val profileImageView: ImageView = view.findViewById(R.id.profileImageView)
        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val phoneNumberTextView = view.findViewById<TextView>(R.id.phoneNumberTextView)
        val birthDayTextView = view.findViewById<TextView>(R.id.birthDayTextView)
        val addressTextView = view.findViewById<TextView>(R.id.addressTextView)

        val documentReference = FirebaseFirestore.getInstance().collection("users").document(
            FirebaseAuth.getInstance().currentUser!!.uid
        )


        documentReference.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    val document: DocumentSnapshot? = task.getResult()
                    if (document != null) {
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.data)
                            if (document.data!!["photoUrl"] != null) {
                                Glide.with(requireActivity()).load(document.data!!["photoUrl"])
                                    .centerCrop().override(500).into(profileImageView)
                            }
                            nameTextView.text = document.data!!["name"].toString()
                            phoneNumberTextView.text = document.data!!["phoneNumber"].toString()
                            birthDayTextView.text = document.data!!["birthDay"].toString()
                            addressTextView.text = document.data!!["address"].toString()
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException())
                }
            }
        // Inflate the layout for this fragment
        return view
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
    }


}
