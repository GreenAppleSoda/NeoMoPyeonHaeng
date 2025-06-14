package com.GreenAppleSoda.neomopyeonhaeng

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductViewModel(private val db: FirebaseFirestore) : ViewModel() {
    // 상품 목록 상태를 StateFlow로 관리
    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>> = _productList

    // 로딩 상태를 StateFlow로 관리
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 에러 메시지 상태를 StateFlow로 관리
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ✨ 새로 추가되는 상태: 선택된 편의점 (초기값은 "전체" 또는 null)
    private val _selectedStore = MutableStateFlow<String>("편의점 선택") // 초기값 "전체"
    val selectedStore: StateFlow<String> = _selectedStore.asStateFlow()

    // ✨ 새로 추가되는 상태: 선택된 행사유형 (초기값은 "행사 전체" 또는 null)
    private val _selectedEvent = MutableStateFlow<String>("행사 전체") // 초기값 "전체"
    val selectedEvent: StateFlow<String> = _selectedEvent.asStateFlow()

    // 모든 편의점 컬렉션 경로를 리스트로 정의
    private val collectionPaths = listOf(
        "GS25_events",
        "CU_events",
        "emart24_events",
        "7-ELEVEN_events"
    )

    // ✨ 필터링된 상품 목록 (원본 목록과 선택된 편의점 상태를 조합)
    // combine을 사용하여 _productList와 _selectedStore가 변경될 때마다 자동 필터링
    val filteredProductList: StateFlow<List<Product>> = combine(
        _productList,
        _selectedStore,
        _selectedEvent
    ) { productList, selectedStore, selectedEvent ->
        var currentFilteredList = productList

        // 1. 편의점 필터링 적용
        if (selectedStore != "전체" && selectedStore != "편의점 선택") {
            currentFilteredList = currentFilteredList.filter { it.store == selectedStore }
        }

        // 2. 행사 유형 필터링 적용
        if (selectedEvent != "행사 전체") {
            currentFilteredList = currentFilteredList.filter { it.event_type == selectedEvent } // Product 모델에 eventType 필드가 있다고 가정
        }

        currentFilteredList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // ViewModel이 생성될 때 데이터를 로드합니다.
        fetchProducts()
    }

    // ✨ 편의점 선택을 업데이트하는 함수
    fun selectStore(store: String) {
        _selectedStore.value = store
        Log.d("ProductDebug", "ProductViewModel: Store selected: $store")
    }

    // ✨ 행사 유형 선택을 업데이트하는 함수 (새로 추가)
    fun selectEvent(event: String) {
        _selectedEvent.value = event
        Log.d("ProductDebug", "ProductViewModel: Event selected: $event")
    }

    // Firestore에서 상품 데이터를 가져오는 함수
    fun fetchProducts() {
        viewModelScope.launch { // viewModelScope를 사용하여 Coroutine 실행
            _isLoading.value = true
            _errorMessage.value = null // 새 로드 시작 시 에러 메시지 초기화
            val allFetchedProducts = mutableListOf<Product>()

            try {
                for (path in collectionPaths) {
                    Log.d("Firestore", "$path 컬렉션에서 데이터 요청 중...")
                    val querySnapshot = db.collection(path).get().await()
                    Log.d("Firestore", "컬렉션 '$path' 문서 수: ${querySnapshot.documents.size}")

                    for (document in querySnapshot) {
                        try {
                            val product = document.toObject<Product>()
                            allFetchedProducts.add(product)
                            // ✨✨✨ 여기에 로그 추가 ✨✨✨
                            Log.d("ProductDebug", "Loaded product: ${product.name}, Store: ${product.store}")
                        } catch (e: Exception) {
                            Log.e("Firestore", "문서 변환 오류 (컬렉션: $path, ID: ${document.id}): ${e.message}", e)

                        }
                    }
                }
                _productList.value = allFetchedProducts
                _isLoading.value = false
                Log.d("Firestore", "총 ${allFetchedProducts.size}개의 상품 데이터 로딩 완료")

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "데이터 로딩 오류: ${e.message}"
                Log.e("Firestore", "데이터 로딩 오류", e)
            }
        }
    }
    // 모든 편의점 이름을 제공하는 함수 (UI에 드롭다운 목록을 보여줄 때 사용)
    fun getAllStoreNames(): List<String> {
        return listOf("전체") + collectionPaths.map { it.substringBefore("_events") }
    }
}

class ProductViewModelFactory(private val db: FirebaseFirestore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}