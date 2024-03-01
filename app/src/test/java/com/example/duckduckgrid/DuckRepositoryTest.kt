package com.example.duckduckgrid

import android.content.SharedPreferences
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DuckRepositoryTest {

    val sharedPrefs: SharedPreferences = Mockito.mock(SharedPreferences::class.java)

    val item: Item = Item()

    @Test
    fun likedIsTrue() {
        sharedPrefs
        with(sharedPrefs.edit()) {
            putBoolean("imgUrl", false)
            commit()
        }
        DuckRepository.toggleLiked(item, sharedPrefs)
        var isStarred = sharedPrefs.getBoolean("imgUrl", false)
        Assert.assertTrue(isStarred)
    }
}