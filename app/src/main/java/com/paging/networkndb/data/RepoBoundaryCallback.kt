package com.paging.networkndb.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.paging.networkndb.api.GithubService
import com.paging.networkndb.api.searchRepos
import com.paging.networkndb.db.GithubLocalCache
import com.paging.networkndb.model.Repo

class RepoBoundaryCallback(
        private val query: String,
        private val service: GithubService,
        private val cache: GithubLocalCache
) : PagedList.BoundaryCallback<Repo>() {

    companion object {
        private const val NETWORK_PAGE_SIZE = 50
    }

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = 1

    // LiveData of network errors.
    private val _networkErrors = MutableLiveData<String>()
    // LiveData of network errors.
    val networkErrors: LiveData<String>
        get() = _networkErrors

    // avoid triggering multiple requests in the same time
    private var _isRequestInProgress = MutableLiveData<Boolean>().apply {this.value=false }
    //LiveData of progress
    val isRequestInProgress: LiveData<Boolean>
        get() = _isRequestInProgress

    override fun onZeroItemsLoaded() {
        requestAndSaveData(query)
    }

    override fun onItemAtEndLoaded(itemAtEnd: Repo) {
        requestAndSaveData(query)
    }


    private fun requestAndSaveData(query: String) {
        if (_isRequestInProgress.value!!) return

        _isRequestInProgress.postValue(true)
        searchRepos(service, query, lastRequestedPage, NETWORK_PAGE_SIZE, { repos ->
            cache.insert(repos) {
                lastRequestedPage++

                //using post value because it'll be called on background thread
                _isRequestInProgress.postValue(false)
            }
        }, { error ->
            _networkErrors.postValue(error)
            _isRequestInProgress.postValue(false)
        })
    }
}