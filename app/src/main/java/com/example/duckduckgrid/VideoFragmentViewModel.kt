package com.example.duckduckgrid

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VideoFragmentViewModel : ViewModel(), CoroutineScope by MainScope() {

    private val _itemList = MutableLiveData<MutableList<Item>>()

    val itemList: LiveData<MutableList<Item>>
        get() = _itemList

    fun initItems() {
        _itemList.value = mutableListOf(
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"),
            Item("https://storage/emulated/0/Download/Screen_recording_20240315_154601.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            Item("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4")
        )
    }


    private suspend fun fetchRandomUrl(callback: (() -> Unit), item: Item) {
        withContext(Dispatchers.Default) {
            val res = URL("https://random-d.uk/api/v2/random").readText()
            item.url = res.split(":", limit = 3)[2].removePrefix("\"").split("\"")[0]
            item.date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString()
            Log.d("DuckDuckDate", item.date ?: "")
            Log.d("DuckDuck", item.url ?: "")

            callback()
        }
    }

    private val callback: (() -> Unit) = {
        _itemList.postValue(_itemList.value?.toMutableList())
    }

    fun loadItems() {
        _itemList.value?.forEach { i ->
            if (i.url == null) {
                launch {
                    fetchRandomUrl(callback, i)
                }
            }
        }
        callback()
    }

    fun addItem() {
        val item = Item()
        launch {
            fetchRandomUrl(callback, item)
        }
        _itemList.value?.add(0, item)
    }

    fun starItem(itemUrl: String, shouldStar: Boolean) {
        val items = _itemList.value

        items?.let {
            it.first { item -> item.url == itemUrl }.liked = shouldStar
            _itemList.postValue(it)
        }
    }
}