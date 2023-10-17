package com.example.duckduckgrid

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duckduckgrid.databinding.FragmentGridBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class FirstFragment : Fragment(),  CoroutineScope by MainScope() {

    private var _binding: FragmentGridBinding? = null

    private val binding get() = _binding!!

    private val viewModel: GridFragmentViewModel by viewModels()

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

        var columns = 2
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {columns = 3}
        val manager = GridLayoutManager(activity, columns, GridLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = manager

        fab.setOnClickListener {
            viewModel.addItem()
        }

        (binding.recyclerView.adapter as? RecyclerViewAdapter)?.let { adapter ->
            adapter.setOnDuckClickListener(object: RecyclerViewAdapter.OnDuckClickListener {
                override fun onClick(position: Int, item: Item) {

                    if (item.url != null && item.date != null) {
                        Log.d("DuckDuck", "WOOOHOOO! " + position )
                        findNavController().navigate(FirstFragmentDirections.actionFirstFragmentToSecondFragment(item.url?:"",item.date?:""))
                    }
                }
            })
        }

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            (binding.recyclerView.adapter as? RecyclerViewAdapter)?.submitList(itemList)
        }

        viewModel.loadItems()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Configuration changes when phone is rotated
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.recyclerView.layoutManager = GridLayoutManager(activity, 2)
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.recyclerView.layoutManager = GridLayoutManager(activity, 3)
        }
    }

}