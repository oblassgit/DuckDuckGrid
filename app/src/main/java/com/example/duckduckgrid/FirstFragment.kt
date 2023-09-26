package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.FragmentFirstBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


data class Item (
    var url: String? = null
) {

    suspend fun fetchRandomUrl(callback: (()->Unit)) {
        withContext(Dispatchers.Default) {
            val res = URL("https://random-d.uk/api/v2/random").readText()
            url = res.split(":", limit = 3)[2].removePrefix("\"").split("\"").get(0)
            Log.d("DuckDuck", url ?:"")

            callback()
        }
    }
}


class FirstFragment : Fragment(),  CoroutineScope by MainScope() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val callback: (() -> Unit) = {
            activity?.runOnUiThread {
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }

        val dataset = mutableListOf(
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

        dataset.forEach{ i ->
            launch {
                i.fetchRandomUrl(callback)
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

    class RecyclerViewAdapter(private val dataSet: List<Item>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
            val imgView: ImageView

            init {
                // Define click listener for the ViewHolder's View
                imgView = view.findViewById(R.id.imgView)
            }

        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.grid_item, viewGroup, false)

            return ViewHolder(view, viewGroup.context)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            Glide.with(viewHolder.imgView.context)
                .load(dataSet[position].url)
                .into(viewHolder.imgView)
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size





    }

}
