package com.example.duckduckgrid

import android.content.Context
import android.content.SharedPreferences
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerViewAdapter :
    ListAdapter<Item, RecyclerViewAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Item>() {


        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return false //oldItem.url == newItem.url //: Ask Gustavo what i should do here. My implementation doesn't work
        }
    }) {

    private var onClickListener: OnDuckClickListener? = null

    class ViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        val imgView: ImageView
        val starBtn: ImageButton
        init {
            // Define click listener for the ViewHolder's View
            imgView = view.findViewById(R.id.imgView)
            starBtn = view.findViewById(R.id.starImgBtn)
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.grid_item, viewGroup, false)

        return ViewHolder(view, viewGroup.context)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val sharedPref: SharedPreferences = viewHolder.context.getSharedPreferences("duckduck", Context.MODE_PRIVATE)
        val item = getItem(position)
        Glide.with(viewHolder.imgView.context)
            .load(item.url)
            .into(viewHolder.imgView)

        viewHolder.itemView.setOnClickListener {
            onClickListener?.onClick(position, item )
        }
        viewHolder.imgView.setOnClickListener {
            onClickListener?.onClick(position, item )
        }
        var isStarred = sharedPref.getBoolean(item.url, false)
        if (isStarred) {
            viewHolder.starBtn.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            viewHolder.starBtn.setImageResource(android.R.drawable.btn_star_big_off)
        }
        item.liked = isStarred
        saveStarred(isStarred, item.url, sharedPref)

        viewHolder.starBtn.setOnClickListener {
            if (isStarred) {
                viewHolder.starBtn.setImageResource(android.R.drawable.btn_star_big_off)
                isStarred = false
                saveStarred(isStarred, item.url, sharedPref)
                item.liked = false
            } else {
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                viewHolder.starBtn.setImageResource(android.R.drawable.btn_star_big_on)
                isStarred = true
                saveStarred(isStarred, item.url, sharedPref)
                item.liked = true
            }
        }
    }

    // A function to bind the onclickListener.
    fun setOnDuckClickListener(onClickListener: OnDuckClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnDuckClickListener {
        fun onClick(position: Int, item: Item)
    }

    private fun saveStarred(isStarred: Boolean, imgUrl: String?, sharedPref: SharedPreferences) {
        sharedPref
        with(sharedPref.edit()) {
            putBoolean(imgUrl, isStarred)
            commit()
        }
    }
}