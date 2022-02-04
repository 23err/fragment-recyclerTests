package com.geekbrains.tests.presenter

import com.geekbrains.tests.model.SearchResponse
import com.geekbrains.tests.repository.RepositoryCallback

interface RepositoryContract {
    fun searchGithub(
        query: String,
        callback: RepositoryCallback
    )

    suspend fun searchGithubAsync(query:String): SearchResponse
}
