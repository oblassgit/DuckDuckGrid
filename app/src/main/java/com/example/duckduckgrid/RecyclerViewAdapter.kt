package com.example.duckduckgrid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.GridItemBinding
import com.example.duckduckgrid.ui.theme.AppTheme


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
        val composeView: ComposeView = binding.composeView

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

            viewHolder.composeView.setContent {
                val shouldStar = bottomSheet(item.url.toString(), viewHolder.context, item.liked)
                if (item.liked != shouldStar) {
                    viewHolder.itemBinding.item = item
                }
                item?.let {
                    onClickListener?.starDuck(item, shouldStar)
                }
            }

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun bottomSheet(url: String, context: Context, _isFavourite: Boolean): Boolean {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        var showBottomSheet by remember { mutableStateOf(true) }
        var isFavourite by remember { mutableStateOf(_isFavourite) }


        AppTheme {
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,
                ) {
                    // Sheet content

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        /*Button(onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }) {
                            Text("Hide bottom sheet")
                        }*/


                        Button(onClick = {
                            isFavourite = !isFavourite

                        }, modifier = Modifier
                            .padding(Dp(10f))
                            .weight(2f)
                            .height(Dp(50f))) {
                            if (isFavourite) {
                                Image(painter = painterResource(id = R.drawable.ic_star_on), contentDescription = "star on", Modifier.height(Dp(26f)))
                            } else {
                                Image(painter = painterResource(id = R.drawable.ic_star_off), contentDescription = "star off", Modifier.height(Dp(26f)))
                            }

                        }

                        Spacer(modifier = Modifier.weight(1f))




                        Button(onClick = {
                            /*val clip = ClipData.newPlainText("img url", url)
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)`*/
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.setType("text/plain")
                            shareIntent.putExtra(Intent.EXTRA_TITLE, "Duck Duck Grid")
                            shareIntent.putExtra(Intent.EXTRA_TEXT, url)
                            startActivity(context, Intent.createChooser(shareIntent, "Share via.."), Bundle.EMPTY)

                            /*scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }*/

                        }, modifier = Modifier
                            .padding(Dp(10f))
                            .weight(2f)
                            .height(Dp(50f))) {
                            Image(painter = rememberVectorPainter(image = Icons.Rounded.Share), contentDescription = "Share", Modifier.height(Dp(26f)))
                        }
                    }


                }
        }

        }
        return isFavourite
    }

    // onClickListener Interface
    interface OnDuckClickListener {
        fun onClick(position: Int, item: Item)

        fun starDuck(item: Item, shouldStar: Boolean)
    }
}