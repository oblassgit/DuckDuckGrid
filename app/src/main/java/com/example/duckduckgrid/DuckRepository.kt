package com.example.duckduckgrid

import android.content.SharedPreferences

class DuckRepository {
    companion object {
        lateinit var sharedPreferences: SharedPreferences

        fun setLikedState(item: Item, sharedPreferences: SharedPreferences, likedState: Boolean) {
            item.liked = likedState
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