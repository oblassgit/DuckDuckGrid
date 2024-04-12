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
            if (isStarred) {
                with(sharedPreferences.edit()) {
                    putBoolean(imgUrl, isStarred)
                    commit()
                }
            } else { //removing entries from shared prefs to save space
                with(sharedPreferences.edit()) {
                    remove(imgUrl)
                    commit()
                }
            }

        }
    }
}