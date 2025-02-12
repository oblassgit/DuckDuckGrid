package com.example.duckduckgrid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duckduckgrid.DuckRepository
import com.example.duckduckgrid.viewmodels.Item
import com.example.duckduckgrid.viewmodels.LikedFragmentViewModel
import com.example.duckduckgrid.adapters.RecyclerViewAdapter
import com.example.duckduckgrid.ViewMode
import com.example.duckduckgrid.compose.bottomSheet
import com.example.duckduckgrid.databinding.FragmentLikedBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.URL

class LikedFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentLikedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LikedFragmentViewModel by viewModels()

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("duckduck", Context.MODE_PRIVATE)
    }

    private val SMALL_MAX_SCALE_FACTOR = 1.25f

    private var viewMode: ViewMode = ViewMode.DEFAULT_VIEW_MODE

    private val recyclerViewAdapter = RecyclerViewAdapter()



    override fun onCreate(savedInstanceState: Bundle?) {
        DuckRepository.sharedPreferences = sharedPreferences
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikedBinding.inflate(inflater, container, false)

        val recyclerView: RecyclerView = binding.recyclerView
        val recyclerViewSmall: RecyclerView = binding.recyclerViewSmall
        recyclerView.adapter = recyclerViewAdapter
        recyclerViewSmall.adapter = recyclerViewAdapter

        val bottomSheetVisibilityFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)


        recyclerView.layoutManager =
            GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)

        recyclerViewSmall.layoutManager =
            GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)

        when (viewMode) {
            ViewMode.BIG -> {
                recyclerView.visibility = View.VISIBLE
                recyclerViewSmall.visibility = View.INVISIBLE
            }

            ViewMode.SMALL -> {
                recyclerViewSmall.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE

            }
        }


        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {

                if (detector.scaleFactor < 1) {
                    recyclerViewSmall.animate().scaleX(1f).scaleY(1f).alpha(1f).withStartAction {
                        recyclerView.animate().scaleY(SMALL_MAX_SCALE_FACTOR)
                            .scaleX(SMALL_MAX_SCALE_FACTOR)
                            .alpha(0f)
                            .start()
                    }.withEndAction { recyclerViewSmall.visibility = View.VISIBLE
                        recyclerViewSmall.bringToFront()
                    }.start()


                    viewMode = ViewMode.SMALL
                    Log.d("ViewMode", "SMALL")


                } else if (detector.scaleFactor > 1) {
                    recyclerView.animate().scaleX(1f).scaleY(1f).alpha(1f).withStartAction {
                        recyclerViewSmall.animate().scaleY(SMALL_MAX_SCALE_FACTOR)
                            .scaleX(SMALL_MAX_SCALE_FACTOR)
                            .alpha(0f)
                            .start()
                    }.withEndAction { recyclerView.visibility = View.VISIBLE
                        recyclerView.bringToFront()
                    }.start()
                    viewMode = ViewMode.BIG
                    Log.d("ViewMode", "BIG")
                }


                return super.onScale(detector)
            }


        }

        val scaleGestureDetector = ScaleGestureDetector(requireActivity(), listener)

        binding.recyclerView.setOnTouchListener { _, event ->
            recyclerViewSmall.scrollToPosition((recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition())
            scaleGestureDetector.onTouchEvent(event)
            false
        }

        binding.recyclerViewSmall.setOnTouchListener { _, event ->
            recyclerView.scrollToPosition((recyclerViewSmall.layoutManager as GridLayoutManager).findFirstVisibleItemPosition())
            scaleGestureDetector.onTouchEvent(event)
            false
        }

        (binding.recyclerView.adapter as? RecyclerViewAdapter)?.setOnDuckClickListener(object :
            RecyclerViewAdapter.OnDuckClickListener {
            override fun onClick(position: Int, item: Item) {

                if (item.url != null && item.date != null) {
                    Log.d("DuckDuck", "WOOOHOOO! $position")
                    findNavController().navigate(
                        LikedFragmentDirections.actionLikedFragmentToSingleImageFragment(
                            item.url ?: "",
                            item.date ?: "",
                            item
                        )
                    )
                }
            }

            override fun onLongClick(position: Int, item: Item) {
                launch {
                    bottomSheetVisibilityFlow.emit(true)

                }
                binding.composeView.setContent {
                    val shouldStar = bottomSheet(URL(item.url), requireContext(), item.liked, bottomSheetVisibilityFlow)

                    if (item.liked != shouldStar) {
                        binding.recyclerView.adapter?.notifyItemChanged(position)
                    }
                    item?.let {
                        starDuck(item, shouldStar)
                    }
                }
            }

            override fun starDuck(item: Item, shouldStar: Boolean) {
                DuckRepository.toggleLiked(item, sharedPreferences)
                item.url?.let {
                    viewModel.starItem(it, shouldStar)
                }
            }
        })

        (binding.recyclerViewSmall.adapter as? RecyclerViewAdapter)?.setOnDuckClickListener(object :
            RecyclerViewAdapter.OnDuckClickListener {
            override fun onClick(position: Int, item: Item) {

                if (item.url != null) {
                    Log.d("DuckDuck", "WOOOHOOO! $position")
                    findNavController().navigate(
                        LikedFragmentDirections.actionLikedFragmentToSingleImageFragment(
                            item.url ?: "",
                            item.date ?: "",
                            item
                        )
                    )
                }
            }

            override fun onLongClick(position: Int, item: Item) {
                launch {
                    bottomSheetVisibilityFlow.emit(true)

                }
                binding.composeView.setContent {
                    val shouldStar = bottomSheet(URL(item.url), requireContext(), item.liked, bottomSheetVisibilityFlow)

                    if (item.liked != shouldStar) {
                        binding.recyclerViewSmall.adapter?.notifyItemChanged(position)
                    }
                    item?.let {
                        starDuck(item, shouldStar)
                    }
                }
            }

            override fun starDuck(item: Item, shouldStar: Boolean) {
                DuckRepository.toggleLiked(item, sharedPreferences)
                item.url?.let {
                    viewModel.starItem(it, shouldStar)
                }
            }
        })

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            (binding.recyclerView.adapter as? RecyclerViewAdapter)?.submitList(itemList)
        }

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            (binding.recyclerViewSmall.adapter as? RecyclerViewAdapter)?.submitList(itemList)
        }

        viewModel.loadItems()

        if (viewModel.itemList.value!!.isEmpty()) {
            binding.noDucksImage.visibility = View.VISIBLE
            binding.noDucksText.visibility = View.VISIBLE
        } else {
            binding.noDucksImage.visibility = View.GONE
            binding.noDucksText.visibility = View.GONE
        }

        return binding.root
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.loadItems()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            viewModel.initItems()
            recyclerViewAdapter.notifyDataSetChanged()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}