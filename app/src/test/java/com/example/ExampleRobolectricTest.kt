package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches islamic quiz`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ইসলামিক কুইজ", appName)
    }

    @Test
    fun `loads all 1000 unique quiz questions from assets`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = QuizRepository(context, db.quizDao(), SettingsDataStore(context))
        val all = repo.getAllQuestions()
        assertEquals(1000, all.size)
        assertEquals(1000, all.map { it.id }.toSet().size)
        assertTrue(all.all { it.opts.size == 4 && it.ans in 0..3 })
    }
}
