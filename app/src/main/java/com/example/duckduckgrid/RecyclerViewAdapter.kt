package com.example.duckduckgrid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.GridItemBinding


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

    class ViewHolder(binding: GridItemBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        val imgView: ImageView
        val starBtn: ImageButton
        val itemBinding: GridItemBinding

            init {
                // Define click listener for the ViewHolder's View
                imgView = binding.imgView
                starBtn = binding.starImgBtn
                itemBinding = binding
            }

        }



    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item

        val binding: GridItemBinding = GridItemBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )

        return ViewHolder(binding, viewGroup.context)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        item.liked = DuckRepository.sharedPreferences.getBoolean(item.url, false)
        Glide.with(viewHolder.imgView.context)
            .load(item.url)
            .into(viewHolder.imgView)

        var favouriteState = if (item.liked) {
            R.attr.state_liked
        } else {
            -R.attr.state_liked
        }
        viewHolder.starBtn.setImageState(
            intArrayOf(favouriteState),
            true
        )



        viewHolder.itemView.setOnClickListener {
            onClickListener?.onClick(position, item)
        }
        viewHolder.imgView.setOnLongClickListener {
            Toast.makeText(viewHolder.context, viewHolder.context.getString(R.string.toast_url_saved_to_clipboard), Toast.LENGTH_SHORT).show()
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            val clip = ClipData.newPlainText("img url", item.url)
            (viewHolder.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
            true
        }
        viewHolder.imgView.setOnClickListener {
            onClickListener?.onClick(position, item)
        }
        viewHolder.itemBinding.item = item


        //2 onclick listeners because of switching between two buttons to achieve different button images
        viewHolder.starBtn.setOnClickListener {
            favouriteState = if (item.liked) {
                -R.attr.state_liked
            } else {
                R.attr.state_liked
            }
            viewHolder.starBtn.setImageState(
                intArrayOf(favouriteState),
                true
            )
            item?.let {
                onClickListener?.starDuck(item, !item.liked)
            }
            viewHolder.itemBinding.item = item
        }


    }

    // A function to bind the onclickListener.
    fun setOnDuckClickListener(onClickListener: OnDuckClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnDuckClickListener {
        fun onClick(position: Int, item: Item)

        fun starDuck(item: Item, shouldStar: Boolean)
    }
}