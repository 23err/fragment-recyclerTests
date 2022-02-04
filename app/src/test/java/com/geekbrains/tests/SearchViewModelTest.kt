package com.geekbrains.tests

import android.app.DownloadManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.geekbrains.tests.model.SearchResponse
import com.geekbrains.tests.model.SearchResult
import com.geekbrains.tests.presenter.search.SearchScreenState
import com.geekbrains.tests.presenter.search.SearchViewModel
import com.geekbrains.tests.repository.FakeGitHubRepository
import com.geekbrains.tests.repository.RepositoryFactory
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.awaitility.kotlin.await
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class SearchViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: SearchViewModel

    @Mock
    private lateinit var repository: FakeGitHubRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun searchGithub_Repo_Invoke_Test() {
        viewModel.searchGitHub(QUERY)
        verify(repository, times(1)).searchGithub(any(), any())
    }

    @Test
    fun liveData_SuccessState_Test() {
        invokeOnLiveData { viewModel, liveData ->
            viewModel.searchGitHub(QUERY)
            val value = liveData.value
            Assert.assertNotNull(value)
            await.atMost(3, TimeUnit.SECONDS).until(
                Callable {
                    return@Callable value!! !is SearchScreenState.Loading
                })
            Assert.assertTrue(value!! is SearchScreenState.Success)
        }
    }

    @Test
    fun liveData_ErrorState_Test() {
        testCoroutineRule.runBlockingTest {
            Mockito.`when`(repository.searchGithubAsync(QUERY))
                .thenReturn(SearchResponse(null, listOf()))
            invokeOnLiveData(viewModel) { viewModel, liveData ->
                viewModel.searchGitHub(QUERY)
                val value = liveData.value
                Assert.assertNotNull(value)
                Assert.assertTrue(value!! is SearchScreenState.Error)
                val error = (value!! as SearchScreenState.Error).error
                Assert.assertEquals(error.message, ERROR_TEXT)
            }
        }
    }

    @Test
    fun liveData_NotNull_Test() {
        testCoroutineRule.runBlockingTest {
            Mockito.`when`(repository.searchGithubAsync(QUERY)).thenReturn(
                SearchResponse(
                    1, listOf(
                        SearchResult(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                )
            )

            invokeOnLiveData(viewModel){viewModel, liveData ->
                viewModel.searchGitHub(QUERY)
                Assert.assertNotNull(liveData.value)

            }
        }
    }

    @Test
    fun nullResponseException_Test(){
        testCoroutineRule.runBlockingTest {
            invokeOnLiveData(viewModel){ viewModel, liveData ->
                viewModel.searchGitHub(QUERY)
                Assert.assertTrue(liveData.value is SearchScreenState.Error)
                val value = liveData.value!! as SearchScreenState.Error
                Assert.assertEquals("Response are null", value.error.message)
            }
        }
    }

    private fun invokeOnLiveData(
        viewModel: SearchViewModel = SearchViewModel(RepositoryFactory.getRepository()),
        invokeFunc: (viewModel: SearchViewModel, liveData: LiveData<SearchScreenState>) -> Unit
    ) {
        val observer = Observer<SearchScreenState> {}
        val liveData = viewModel.liveData()
        liveData.observeForever(observer)
        try {
            invokeFunc.invoke(viewModel, liveData)
        } finally {
            liveData.removeObserver(observer)
        }
    }

    companion object {
        private const val QUERY = "egol"
        private const val ERROR_TEXT = "Search results or total count are null"
    }
}