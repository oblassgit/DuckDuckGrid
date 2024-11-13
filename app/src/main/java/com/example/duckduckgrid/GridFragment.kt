package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duckduckgrid.databinding.FragmentGridBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.net.URL

class GridFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GridFragmentViewModel by viewModels()

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("duckduck", Context.MODE_PRIVATE)
    }

    private val SMALL_MAX_SCALE_FACTOR = 1.25f

    private var viewMode: ViewMode = ViewMode.DEFAULT_VIEW_MODE

    private val recyclerViewAdapter = RecyclerViewAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        loadItems()
        DuckRepository.sharedPreferences = sharedPreferences
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadItems()
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setColorSchemeColors(requireContext().getColorFromAttr(com.google.android.material.R.attr.colorPrimary))
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(requireContext().getColorFromAttr(com.google.android.material.R.attr.colorButtonNormal))

        val recyclerView: RecyclerView = binding.recyclerView
        val recyclerViewSmall: RecyclerView = binding.recyclerViewSmall
        recyclerView.adapter = recyclerViewAdapter
        recyclerViewSmall.adapter = recyclerViewAdapter

        val fab: FloatingActionButton = binding.addDuckBtn

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

        fab.setOnClickListener {
            recyclerView.scrollToPosition(0)
            recyclerViewSmall.scrollToPosition(0)
            addItem()
        }
        loadItems()


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
                    }.withEndAction {
                        recyclerView.visibility = View.VISIBLE
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
                        GridFragmentDirections.actionFirstFragmentToSecondFragment(
                            item.url ?: "",
                            item.date ?: "",
                            item
                        )
                    )
                }
            }

            override fun onLongClick(position: Int, item: Item) {
                var shouldStar = false
                binding.composeView.setContent {
                    shouldStar = bottomSheet(URL(item.url), requireContext(), item.liked)
                    showModalBottomSheet
                }
                if (item.liked != shouldStar) {
                    binding.recyclerView.adapter?.notifyItemChanged(position)
                }
                item?.let {
                    starDuck(item, shouldStar)
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

            override fun onLongClick(position: Int, item: Item) {
                binding.composeView.setContent {
                    val shouldStar = bottomSheet(URL(item.url), requireContext(), item.liked)
                    showModalBottomSheet
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
            // (binding.recyclerView.adapter as? RecyclerViewAdapter)?.submitList(itemList)
            for ((i, item) in itemList.withIndex()) {
                val item = item as? Item
                item?.let { item ->
                    if (!item.url.equals(item.lastCheckedUrl)) {
                        binding.recyclerViewSmall.adapter?.notifyItemChanged(i)
                        item.lastCheckedUrl = item.url
                    }
                }
            }
        }

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            (binding.recyclerViewSmall.adapter as? RecyclerViewAdapter)?.submitList(itemList)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadItems()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadItems()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            viewModel.initItems()
            recyclerViewAdapter.notifyDataSetChanged()
        }
        loadItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isNetworkAvailable(): Boolean {
        val conectivityManager: ConnectivityManager = context?.getSystemService()!!
        return conectivityManager.getNetworkCapabilities(conectivityManager.activeNetwork)
            .isNetworkCapabilitiesValid()
    }

    private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean = when {
        this == null -> false
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true

        else -> false
    }

    private fun loadItems() {
        if (!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(),
                "No active Internet connection available",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.loadItems()
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    private fun addItem() {
        if (!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(),
                "No active Internet connection available",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.addItem()
            recyclerViewAdapter.notifyItemInserted(0)
        }

    }

    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}