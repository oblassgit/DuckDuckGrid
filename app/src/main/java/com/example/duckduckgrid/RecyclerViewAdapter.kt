package com.example.duckduckgrid

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.compose.material3.ExperimentalMaterial3Api
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
            return oldItem.url == newItem.url //: Ask Gustavo what i should do here. My implementation doesn't work
        }
    }) {

    private var onClickListener: OnDuckClickListener? = null

    class ViewHolder(binding: GridItemBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        val imgView: ImageView
        val starBtnOff: ImageButton
        val starBtnOn: ImageButton
        val itemBinding: GridItemBinding

        init {
                // Define click listener for the ViewHolder's View
                imgView = binding.imgView
                starBtnOff = binding.starImgBtn
                starBtnOn = binding.starImgBtnActive
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        Glide.with(viewHolder.imgView.context)
            .load(item.url)
            .into(viewHolder.imgView)



        viewHolder.itemView.setOnClickListener {
            onClickListener?.onClick(position, item)
        }

        viewHolder.imgView.setOnLongClickListener {
            onClickListener?.onLongClick(position, item)

            /*Toast.makeText(viewHolder.context, viewHolder.context.getString(R.string.toast_url_saved_to_clipboard), Toast.LENGTH_SHORT).show()
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            val clip = ClipData.newPlainText("img url", item.url)
            (viewHolder.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)*/
            true
        }
        viewHolder.imgView.setOnClickListener {
            onClickListener?.onClick(position, item)
        }
        var isStarred = item.liked
        item.liked = isStarred
        viewHolder.itemBinding.item = item


        //2 onclick listeners because of switching between two buttons to achieve different button images
        viewHolder.starBtnOff.setOnClickListener {
            item?.let {
                onClickListener?.starDuck(item, true)
            }
            viewHolder.itemBinding.item = item
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }

        viewHolder.starBtnOn.setOnClickListener {
            //DuckRepository.toggleLiked(item, sharedPref)
            item?.let {
                onClickListener?.starDuck(it, false)
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

        fun onLongClick(position: Int, item: Item)

        fun starDuck(item: Item, shouldStar: Boolean)
    }
}