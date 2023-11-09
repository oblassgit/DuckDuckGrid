package com.example.duckduckgrid

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.duckduckgrid.databinding.FragmentGridBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class GridFragment : Fragment(), CoroutineScope by MainScope() {
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GridFragmentViewModel by viewModels()

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("duckduck", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGridBinding.inflate(inflater, container, false)

        val recyclerViewAdapter = RecyclerViewAdapter()

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = recyclerViewAdapter

        val fab: FloatingActionButton = binding.addDuckBtn

        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }

        fab.setOnClickListener {
            viewModel.addItem()
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

        viewModel.loadItems()

        return binding.root
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