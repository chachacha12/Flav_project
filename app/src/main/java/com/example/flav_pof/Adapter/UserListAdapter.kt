package com.example.flav_pof.Adapter

import android.R
import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.flav_pof.UserInfo


class UserListAdapter(
    var activity: Activity,
    var mDataset: ArrayList<UserInfo>
): RecyclerView.Adapter<UserListAdapter.MainViewHolder>() {

    class MainViewHolder(var cardView: CardView) : RecyclerView.ViewHolder(
        cardView
    )

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(com.example.flav_pof.R.layout.item_user_list, parent, false) as CardView
        val mainViewHolder = MainViewHolder(cardView)
        cardView.setOnClickListener {
            //

        }
        return mainViewHolder
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val cardView = holder.cardView
        val photoImageVIew: ImageView = cardView.findViewById(com.example.flav_pof.R.id.photoImageVIew)
        val nameTextView = cardView.findViewById<TextView>(com.example.flav_pof.R.id.nameTextView)
        val addressTextView = cardView.findViewById<TextView>(com.example.flav_pof.R.id.addressTextView)
        val userInfo = mDataset!![position]
        if (mDataset!![position].photoUrl != null) {
            Glide.with(activity!!).load(mDataset!![position].photoUrl).centerCrop().override(500)
                .into(photoImageVIew)
        }
        nameTextView.setText(userInfo.name)
        addressTextView.setText(userInfo.address)
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}