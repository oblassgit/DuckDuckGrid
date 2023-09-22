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
import com.example.duckduckgrid.databinding.FragmentFirstBinding
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import javax.security.auth.callback.Callback

data class Item (
    val img: Int)

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


        val dataset = arrayOf(
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),
            Item(R.drawable.img1),

        )
        val customAdapter = CustomAdapter(dataset)

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = customAdapter

        val manager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = manager


        getRandomDuckUri()
        return binding.root
    }

    private fun getRandomDuckUri() {
        val request = okhttp3.Request.Builder()
            .url("https://random-d.uk/api/v2/random")
            .build()

        client.newCall(request).enqueue(object : Callback, okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                Log.d("results: ", response.body!!.string())
                val responseStrings = response.toString().split(",", limit = 2)
                val imgUrl = responseStrings.get(1).split(":", limit = 2).get(1).removePrefix("\"").removeSuffix("\"")
                Log.d("url: ",imgUrl)

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CustomAdapter(private val dataSet: Array<Item>) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

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

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.imgView.setImageDrawable(viewHolder.context.getDrawable(dataSet[position].img))
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size



    }




}