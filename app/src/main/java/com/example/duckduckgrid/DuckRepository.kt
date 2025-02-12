package com.example.duckduckgrid

import android.content.SharedPreferences
import com.example.duckduckgrid.viewmodels.Item

class DuckRepository {
    companion object {
        lateinit var sharedPreferences: SharedPreferences

        fun toggleLiked(item: Item, sharedPreferences: SharedPreferences) {
            item.liked = !item.liked
            saveStarred(item.liked, sharedPreferences, item.url)
        }

        private fun saveStarred(isStarred: Boolean, sharedPreferences: SharedPreferences, imgUrl: String?) {
            with(sharedPreferences.edit()) {
                if (isStarred) {
                    putBoolean(imgUrl, isStarred)
                } else {
                    remove(imgUrl)
                }
                commit()
            }
        }
    }
}