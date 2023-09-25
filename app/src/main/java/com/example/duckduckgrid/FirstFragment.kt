package com.example.duckduckgrid

import android.content.Context
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.net.URL
import javax.security.auth.callback.Callback


data class Item (
    val callback: (()->Unit),
    var url: String? = null
) {

    init {
        GlobalScope.launch {
            val res = URL("https://random-d.uk/api/v2/random").readText()
            url = res.split(":", limit = 3)[2].removePrefix("\"").split("\"").get(0)
            Log.d("DuckDuck", url ?:"")

            callback()
        }
    }
}


class FirstFragment : Fragment() {

    private val client = OkHttpClient()

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val callback: (() -> Unit) = {
            activity?.runOnUiThread {
                binding.recyclerView?.adapter?.notifyDataSetChanged()
            }
        }

        val dataset = mutableListOf(
            Item(callback),
            Item(callback),
            Item(callback),
            Item(callback),
            Item(callback),
            Item(callback),
        )

        val recyclerViewAdapter = RecyclerViewAdapter(dataset)

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = recyclerViewAdapter

        val fab: FloatingActionButton = binding.addDuckBtn


        val manager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = manager

        fab.setOnClickListener {
            dataset.add(0,Item(callback))
        }

        return binding.root
    }

    private fun getRandomDuckUrl(): String {
        val request = okhttp3.Request.Builder()
            .url("https://random-d.uk/api/v2/random")
            .build()
        var url = ""
        client.newCall(request).enqueue(object : Callback, okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {

                url = response.body!!.string().split(":", limit = 3).get(2).removePrefix("\"").split("\"").get(0)
                Log.d("duckUrl", url)

            }

        })
        return url
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
