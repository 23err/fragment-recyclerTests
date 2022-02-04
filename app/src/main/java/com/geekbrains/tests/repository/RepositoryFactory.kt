package com.geekbrains.tests.repository

import com.geekbrains.tests.BuildConfig
import com.geekbrains.tests.presenter.RepositoryContract
import com.geekbrains.tests.view.search.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RepositoryFactory {
    fun getRepository(): RepositoryContract {
        return if (BuildConfig.TYPE == MainActivity.FAKE) {
            FakeGitHubRepository()
        } else {
            GitHubRepository(createRetrofit().create(GitHubApi::class.java))
        }
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MainActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}