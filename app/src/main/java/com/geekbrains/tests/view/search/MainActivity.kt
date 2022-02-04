package com.geekbrains.tests.view.search

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.geekbrains.tests.R
import com.geekbrains.tests.model.SearchResult
import com.geekbrains.tests.presenter.search.SearchScreenState
import com.geekbrains.tests.presenter.search.SearchViewModel
import com.geekbrains.tests.view.details.DetailsActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val adapter = SearchResultAdapter()
    private val viewModel by lazy { ViewModelProvider(this).get(SearchViewModel::class.java) }
    private var totalCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.liveData().observe(this) { handleResult(it) }
        setUI()
    }

    private fun handleResult(state: SearchScreenState) {
        when (state) {
            is SearchScreenState.Loading -> {
                displayLoading(true)
            }
            is SearchScreenState.Error -> {
                displayLoading(false)
                displayError(state.error.message ?: "Неизвестная ошибка")
            }
            is SearchScreenState.Success -> {
                displayLoading(false)
                displaySearchResults(state.searchResponse, state.totalCount)
            }
        }
    }

    private fun setUI() {
        toDetailsActivityButton.setOnClickListener {
            startActivity(DetailsActivity.getIntent(this, totalCount))
        }
        setQueryListener()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun setQueryListener() {
        searchEditText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString()
                if (query.isNotBlank()) {
                    viewModel.searchGitHub(query)
                    return@OnEditorActionListener true
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.enter_search_word),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnEditorActionListener false
                }
            }
            false
        })
    }

    fun displaySearchResults(
        searchResults: List<SearchResult>,
        totalCount: Int
    ) {
        with(totalCountTextView) {
            visibility = View.VISIBLE
            text =
                String.format(Locale.getDefault(), getString(R.string.results_count), totalCount)
        }

        this.totalCount = totalCount
        adapter.updateResults(searchResults)
    }

    fun displayError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    fun displayLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val BASE_URL = "https://api.github.com"
        const val FAKE = "FAKE"
    }
}
