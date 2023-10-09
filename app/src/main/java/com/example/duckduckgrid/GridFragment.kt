package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duckduckgrid.databinding.FragmentGridBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class Item (
    var url: String? = null,
    var date: String? = null
) {

    suspend fun fetchRandomUrl(callback: (()->Unit)) {
        withContext(Dispatchers.Default) {
            val res = URL("https://random-d.uk/api/v2/random").readText()
            url = res.split(":", limit = 3)[2].removePrefix("\"").split("\"").get(0)
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString()
            Log.d("DuckDuckDate", date?:"")
            Log.d("DuckDuck", url ?:"")

            callback()
        }
    }
}


class FirstFragment : Fragment(),  CoroutineScope by MainScope() {

    private var _binding: FragmentGridBinding? = null

    private val binding get() = _binding!!

    private val dataset = mutableListOf(
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
        Item(),
    )

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGridBinding.inflate(inflater, container, false)

        val callback: (() -> Unit) = {
            activity?.runOnUiThread {
                try {
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                } catch (e: NullPointerException) {
                    Log.d("catch", "Nullpointer catched!")
                }

            }
        }

        dataset.forEach{ i ->
            if(i.url == null) {
                launch {
                    i.fetchRandomUrl(callback)
                }
            }
        }

        val recyclerViewAdapter = RecyclerViewAdapter(dataset)

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = recyclerViewAdapter

        val fab: FloatingActionButton = binding.addDuckBtn

        var columns = 2
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {columns = 3}
        val manager = GridLayoutManager(activity, columns, GridLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = manager

        fab.setOnClickListener {
            val item = Item()
            launch {
                item.fetchRandomUrl(callback)
            }

            dataset.add(0,item)
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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


