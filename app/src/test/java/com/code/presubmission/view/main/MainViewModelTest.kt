package com.code.presubmission.view.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.code.presubmission.Dummy
import com.code.presubmission.MainDispatcherRule
import com.code.presubmission.data.UserRepository
import com.code.presubmission.data.response.ListStoryItem
import com.code.presubmission.getOrAwaitValue
import com.code.presubmission.view.adapter.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository
    private val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLUJXTjFvdWFBNldSeGxXVjUiLCJpYXQiOjE2OTg1NTQ3ODh9.QD4leP48wilqr-H7tMdQhroX-oYi4FOGRUmHfiKMLis"

    @SuppressLint("CheckResult")
    @Test
    fun `when Get Stories Should Not Null and Return Success`() = runTest {
        val dummyAllStoriesResponse = Dummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> =
            StoryPagingStore.snapshot(dummyAllStoriesResponse.listStory)
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = data


        Mockito.mockStatic(Log::class.java)
        Mockito.`when`(userRepository.getStory(token)).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(userRepository)
        val actualStories: PagingData<ListStoryItem> = mainViewModel.getStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        assertNotNull(differ.snapshot())
        assertEquals(dummyAllStoriesResponse.listStory, differ.snapshot())
        assertEquals(dummyAllStoriesResponse.listStory.size, differ.snapshot().size)
        assertEquals(dummyAllStoriesResponse.listStory[0], differ.snapshot()[0])

    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = data

        Mockito.`when`(userRepository.getStory(token)).thenReturn(expectedStories)
        val mainViewModel = MainViewModel(userRepository)
        val actualStories: PagingData<ListStoryItem> = mainViewModel.getStory(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        assertEquals(0, differ.snapshot().size)
    }
}

class StoryPagingStore : PagingSource<Int, LiveData<List<ListStoryItem>>>() {

    companion object {
        fun snapshot(listStory: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(listStory)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}