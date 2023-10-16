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
import java.util.UUID

data class Item (
    var url: String? = null,
    var date: String? = null,
    val id: UUID = UUID.randomUUID()
) {

    suspend fun fetchRandomUrl(callback: (()->Unit)) {
        withContext(Dispatchers.Default) {
            val res = URL("https://random-d.uk/api/v2/random").readText()
            url = res.split(":", limit = 3)[2].removePrefix("\"").split("\"")[0]
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString()
            Log.d("DuckDuckDate", date?:"")
            Log.d("DuckDuck", url ?:"")

            callback()
        }
    }
}

class GridFragmentViewModel : ViewModel(),  CoroutineScope by MainScope()  {

    private val _itemList = MutableLiveData<MutableList<Item>>()
    val itemList: LiveData<MutableList<Item>>
        get() = _itemList

    init {
        _itemList.value =  mutableListOf(
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

    private val callback: (() -> Unit) = {
        _itemList.postValue(_itemList.value?.toMutableList())
    }
    fun loadItems() {
        _itemList.value?.forEach{ i ->
            if(i.url == null) {
                launch {
                    i.fetchRandomUrl(callback)
                }
            }
        }
        callback()
    }

    fun addItem() {
        val item = Item()
        launch {
            item.fetchRandomUrl(callback)
        }
        _itemList.value?.add(0,item)
    }
}