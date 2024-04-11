package com.example.duckduckgrid

import android.content.SharedPreferences

class DuckRepository {
    companion object {
        lateinit var sharedPreferences: SharedPreferences

        fun toggleLiked(item: Item, sharedPreferences: SharedPreferences) {
            item.liked = !item.liked
            saveStarred(item.liked, sharedPreferences, item.url)
        }

        private fun saveStarred(isStarred: Boolean, sharedPreferences: SharedPreferences, imgUrl: String?) {
            with(sharedPreferences.edit()) {
                putBoolean(imgUrl, isStarred)
                commit()
            }
        }
    }
}