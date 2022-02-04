package com.geekbrains.tests.presenter.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekbrains.tests.model.SearchResponse
import com.geekbrains.tests.model.SearchResult
import com.geekbrains.tests.presenter.RepositoryContract
import com.geekbrains.tests.repository.GitHubRepository
import com.geekbrains.tests.repository.RepositoryCallback
import com.geekbrains.tests.repository.RepositoryFactory
import kotlinx.coroutines.*
import retrofit2.Response

class SearchViewModel(
    private val repository: RepositoryContract = RepositoryFactory.getRepository()
) : ViewModel() {

    private val liveData = MutableLiveData<SearchScreenState>()
    fun liveData(): LiveData<SearchScreenState> = liveData
    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable -> handleError(throwable) })

    private fun handleError(throwable: Throwable) {
        liveData.postValue(SearchScreenState.Error(Throwable(throwable.message ?: "Response are null")))
    }

    fun searchGitHub(searchQuery: String) {
        liveData.postValue(SearchScreenState.Loading)
        viewModelCoroutineScope.launch {
            val response = repository.searchGithubAsync(searchQuery)
            val searchResults = response.searchResults
            val totalCount = response.totalCount
            if (searchResults != null && totalCount != null) {
                liveData.postValue(SearchScreenState.Success(searchResults, totalCount))
            } else {
                liveData.postValue(SearchScreenState.Error(Throwable("Search results or total count are null")))
            }

        }
    }
}

sealed class SearchScreenState {
    object Loading : SearchScreenState()
    data class Success(val searchResponse: List<SearchResult>, val totalCount: Int) :
        SearchScreenState()

    data class Error(val error: Throwable) : SearchScreenState()
}
