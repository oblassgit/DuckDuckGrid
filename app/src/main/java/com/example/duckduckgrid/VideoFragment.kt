package com.example.duckduckgrid

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
import com.example.duckduckgrid.databinding.FragmentVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class VideoFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideoFragmentViewModel by viewModels()

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("duckduck", Context.MODE_PRIVATE)
    }

    private val SMALL_MAX_SCALE_FACTOR = 1.25f

    private var viewMode: ViewMode = ViewMode.DEFAULT_VIEW_MODE

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)

        val recyclerViewAdapter = VideoRecyclerViewAdapter()

        val recyclerView: RecyclerView = binding.recyclerView
        val recyclerViewSmall: RecyclerView = binding.recyclerViewSmall
        recyclerView.adapter = recyclerViewAdapter
        recyclerViewSmall.adapter = recyclerViewAdapter

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
                    }.withEndAction {
                        recyclerViewSmall.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    }.start()


                    viewMode = ViewMode.SMALL

                } else if (detector.scaleFactor > 1) {
                    recyclerView.animate().scaleX(1f).scaleY(1f).alpha(1f).withStartAction {
                        recyclerViewSmall.animate().scaleY(SMALL_MAX_SCALE_FACTOR)
                            .scaleX(SMALL_MAX_SCALE_FACTOR)
                            .alpha(0f)
                            .start()
                    }.withEndAction {
                        recyclerView.visibility = View.VISIBLE
                        recyclerViewSmall.visibility = View.INVISIBLE
                    }.start()
                    viewMode = ViewMode.BIG
                }


                Log.d("visibility", "normal ${recyclerView.visibility}")
                Log.d("visibility", "small ${recyclerViewSmall.visibility}")

                Log.d("alpha", "normal ${recyclerView.alpha}")
                Log.d("alpha", "small ${recyclerViewSmall.alpha}")


                Log.d("listener", "current ${detector.currentSpan}")
                Log.d("listener", "scalefactor ${detector.scaleFactor}")
                Log.d("listener", "previous ${detector.previousSpan}")

                return super.onScale(detector)
            }


        }

        val scaleGestureDetector = ScaleGestureDetector(requireActivity(), listener)

        binding.recyclerView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            false
        }

        binding.recyclerViewSmall.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            false
        }

        (binding.recyclerView.adapter as? VideoRecyclerViewAdapter)?.setOnDuckClickListener(object :
            VideoRecyclerViewAdapter.OnDuckClickListener {
            override fun onClick(position: Int, item: Item) {

                if (item.url != null && item.date != null) {
                    Log.d("DuckDuck", "WOOOHOOO! $position")
                    findNavController().navigate(
                        GridFragmentDirections.actionFirstFragmentToSecondFragment(
                            item.url ?: "",
                            item.date ?: "",
                            item
                        )
                    )
                }
            }

            override fun starDuck(item: Item, shouldStar: Boolean) {
                DuckRepository.toggleLiked(item, sharedPreferences)
                item.url?.let {
                    viewModel.starItem(it, shouldStar)
                }
            }
        })

        (binding.recyclerViewSmall.adapter as? VideoRecyclerViewAdapter)?.setOnDuckClickListener(
            object :
                VideoRecyclerViewAdapter.OnDuckClickListener {
                override fun onClick(position: Int, item: Item) {

                    if (item.url != null && item.date != null) {
                        Log.d("DuckDuck", "WOOOHOOO! $position")
                        findNavController().navigate(
                            GridFragmentDirections.actionFirstFragmentToSecondFragment(
                                item.url ?: "",
                                item.date ?: "",
                                item
                            )
                        )
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
            (binding.recyclerView.adapter as? VideoRecyclerViewAdapter)?.submitList(itemList)
        }

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            (binding.recyclerViewSmall.adapter as? VideoRecyclerViewAdapter)?.submitList(itemList)
        }

        viewModel.loadItems()

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
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}