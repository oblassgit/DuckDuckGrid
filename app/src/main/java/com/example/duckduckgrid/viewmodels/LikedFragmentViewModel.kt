package com.example.duckduckgrid.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.duckduckgrid.DuckRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID


class LikedFragmentViewModel : ViewModel(), CoroutineScope by MainScope() {

    private val _itemList = MutableLiveData<MutableList<Item>>()

    private val sharedPreferences = DuckRepository.sharedPreferences

    val itemList: LiveData<MutableList<Item>>
        get() = _itemList

    fun initItems() {
        _itemList.value = mutableListOf(

        )
        populateList()

    }

    private val callback: (() -> Unit) = {
        _itemList.postValue(_itemList.value?.toMutableList())
    }

    fun populateList() {
        val all = sharedPreferences.all
        all.forEach{ i ->
        if ((i.value as Boolean)) {
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString()
            _itemList.value?.add(Item(i.key, null,date, UUID.randomUUID(), true))

            }
        }


    }

    fun loadItems() {
        launch {
            populateList()
        }
        callback()
    }

    fun starItem(itemUrl: String, shouldStar: Boolean) {
        val items = _itemList.value

        items?.let {
            it.first { item -> item.url == itemUrl }.liked = shouldStar
            _itemList.postValue(it)
        }
    }
}