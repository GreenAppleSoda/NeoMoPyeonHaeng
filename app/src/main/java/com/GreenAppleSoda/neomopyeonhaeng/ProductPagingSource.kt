package com.GreenAppleSoda.neomopyeonhaeng

import android.graphics.pdf.LoadParams
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class ProductPagingSource(
    private val db: FirebaseFirestore,
    private val collectionPath: String,
    private val selectedStore: String, // 필터링을 위한 추가 인자
    private val selectedEvent: String // 필터링을 위한 추가 인자
) : PagingSource<QuerySnapshot, Product>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Product>): QuerySnapshot? {
        // 새로고침 키 (현재 스크롤 위치에서 다시 로드할 때 사용)
        // 일반적으로 null을 반환하거나, 복잡한 경우 refreshKey를 구현합니다.
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Product> {
        return try {
            val pageSize = params.loadSize.toLong() // 한 번에 로드할 아이템 수
            var query: Query = db.collection(collectionPath)

            // 필터링 적용 (selectedStore, selectedEvent를 ProductViewModel에서 PagingSource로 전달)
            if (selectedStore != "전체" && selectedStore != "편의점 선택") {
                query = query.whereEqualTo("store", selectedStore)
            }
            if (selectedEvent != "행사 전체") {
                query = query.whereEqualTo("event_type", selectedEvent)
            }

            // LoadParams.key (이전 페이지의 마지막 스냅샷)을 사용하여 다음 페이지 시작점 지정
            val currentPage = params.key
            if (currentPage != null) {
                query = query.startAfter(currentPage.documents.last())
            }

            val snapshot = query.limit(pageSize).get().await() // Firestore 쿼리 실행
            val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }

            LoadResult.Page(
                data = products,
                prevKey = null, // 뒤로는 로드하지 않으므로 null
                nextKey = if (snapshot.isEmpty || products.size < pageSize) null else snapshot // 다음 페이지가 없으면 null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}