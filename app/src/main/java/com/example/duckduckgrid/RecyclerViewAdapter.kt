package com.example.duckduckgrid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerViewAdapter(private val dataSet: List<Item>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    var onClickListener: OnDuckClickListener? = null

    class ViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        val imgView: ImageView

        init {
            // Define click listener for the ViewHolder's View
            imgView = view.findViewById(R.id.imgView)
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
        Glide.with(viewHolder.imgView.context)
            .load(dataSet[position].url)
            .into(viewHolder.imgView)
        viewHolder.itemView.setOnClickListener {
            onClickListener?.onClick(position, dataSet[position] )
        }
        viewHolder.imgView.setOnClickListener {
            onClickListener?.onClick(position, dataSet[position] )
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    // A function to bind the onclickListener.
    fun setOnDuckClickListener(onClickListener: OnDuckClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnDuckClickListener {
        fun onClick(position: Int, item: Item)
    }
}