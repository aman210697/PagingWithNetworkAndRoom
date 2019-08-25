package com.paging.networkndb.data

import android.util.Log
import androidx.paging.LivePagedListBuilder
import com.paging.networkndb.api.GithubService
import com.paging.networkndb.db.GithubLocalCache
import com.paging.networkndb.model.RepoSearchResult

/**
 * Repository class that works with local and remote data sources.
 */
class GithubRepository(
        private val service: GithubService,
        private val cache: GithubLocalCache
) {

    /**
     * Search repositories whose names match the query.
     */
    fun search(query: String): RepoSearchResult {
        Log.d("GithubRepository", "New query: $query")

        // Get data source factory from the local cache
        val dataSourceFactory = cache.reposByName(query)

        // Construct the boundary callback
        val boundaryCallback = RepoBoundaryCallback(query, service, cache)
        val networkErrors = boundaryCallback.networkErrors
        val loadingState = boundaryCallback.isRequestInProgress

        // Get the paged list
        val data = LivePagedListBuilder(dataSourceFactory, DATABASE_PAGE_SIZE)
                .setBoundaryCallback(boundaryCallback)
                .build()


        // Get the network errors exposed by the boundary callback
        return RepoSearchResult(data, networkErrors,loadingState)
    }

    companion object {
        private const val DATABASE_PAGE_SIZE = 20
    }
}