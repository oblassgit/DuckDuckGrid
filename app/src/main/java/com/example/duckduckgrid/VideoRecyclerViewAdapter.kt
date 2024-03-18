package com.example.duckduckgrid

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.VideoView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.example.duckduckgrid.databinding.VideoGridItemBinding

class VideoRecyclerViewAdapter :
    ListAdapter<Item, VideoRecyclerViewAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return false//oldItem.url == newItem.url //: Ask Gustavo what i should do here. My implementation doesn't work
        }
    }) {

    private var onClickListener: VideoRecyclerViewAdapter.OnDuckClickListener? = null


    class ViewHolder(binding: VideoGridItemBinding, val context: Context) :
            RecyclerView.ViewHolder(binding.root) {
            val vidView: VideoView
            val starBtnOff: ImageButton
            val starBtnOn: ImageButton
            val itemBinding: VideoGridItemBinding

            init {
                // Define click listener for the ViewHolder's View
                vidView = binding.vidView
                starBtnOff = binding.starImgBtn
                starBtnOn = binding.starImgBtnActive
                itemBinding = binding
            }

        }



        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item

            val binding: VideoGridItemBinding = VideoGridItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return ViewHolder(binding, viewGroup.context)
        }



    // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val item = getItem(position)

            viewHolder.vidView.setVideoURI(Uri.parse(item.url))
            viewHolder.vidView.start()


            Log.d("Video is playing", viewHolder.vidView.isPlaying.toString())
            Log.d("Video URI", Uri.parse(item.url).toString())


            var isStarred = item.liked
            item.liked = isStarred
            viewHolder.itemBinding.item = item

            viewHolder.itemView.setOnClickListener {
                onClickListener?.onClick(position, item)
            }
            viewHolder.vidView.setOnClickListener {
                onClickListener?.onClick(position, item)
            }


            //2 onclick listeners because of switching between two buttons to achieve different button images
            viewHolder.starBtnOff.setOnClickListener {

                viewHolder.itemBinding.item = item
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }

            viewHolder.starBtnOn.setOnClickListener {
                //DuckRepository.toggleLiked(item, sharedPref)

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