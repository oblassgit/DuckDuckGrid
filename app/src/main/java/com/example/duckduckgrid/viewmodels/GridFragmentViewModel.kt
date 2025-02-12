package com.example.duckduckgrid.viewmodels

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class Item(
    var url: String? = null,
    var lastCheckedUrl: String? = null,
    var date: String? = null,
    val id: UUID = UUID.randomUUID(),
    var liked: Boolean = false
) : Parcelable

class GridFragmentViewModel : ViewModel(), CoroutineScope by MainScope() {

    private val _itemList = MutableLiveData<List<Item>>()
    val itemList: LiveData<List<Item>>
        get() = _itemList


    fun initItems() {
        _itemList.value = mutableListOf(
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
    }

    private var pollingJob: Job? = null


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

    suspend fun addItem() {
        val item = Item()
        withContext(Dispatchers.Main) {
            fetchRandomUrl(callback, item)
            _itemList.value = listOf(item) + (_itemList.value ?: emptyList())
        }
    }

    fun starItem(itemUrl: String, shouldStar: Boolean) {
        val items = _itemList.value

        items?.let {
            it.first { item -> item.url == itemUrl }.liked = shouldStar
            _itemList.postValue(it)
        }
    }

    fun startPolling(intervalMillis: Long) {
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    addItem()
                } catch (e: Exception) {
                    Log.e("Polling", "Error fetching data: ${e.message}")
                }
                delay(intervalMillis) // Wait before the next API call
            }
        }
    }

    fun cancelPolling() {
        pollingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelPolling()
    }
}