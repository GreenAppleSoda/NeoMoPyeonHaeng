package com.GreenAppleSoda.neomopyeonhaeng


import android.location.Location
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.GreenAppleSoda.neomopyeonhaeng.Place
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MapViewModel : ViewModel() { // 기존과 동일
    private val _places = mutableStateOf<List<Place>>(emptyList())
    val places: State<List<Place>> = _places

    // 사용자 위치가 업데이트될 때마다 거리를 다시 계산하기 위한 State
    private val _userLocation = mutableStateOf<LatLng?>(null)
    var userLocation: LatLng?
        get() = _userLocation.value
        set(value) {
            if (_userLocation.value != value) { // 위치가 변경되었을 때만 업데이트
                _userLocation.value = value
                calculateDistances() // 사용자 위치가 업데이트되면 거리 재계산
            }
        }

    init {
        fetchPlacesFromFirebase()
    }

    private fun fetchPlacesFromFirebase() {
        val db = Firebase.firestore
        db.collection("편의점")
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                Log.d("MapViewModel", "총 문서 수: ${result.size()}")

                val placeList = result.mapNotNull { document ->
                    Log.d("MapViewModel", "문서 데이터: ${document.data}")

                    val locationMap = document.get("위치") as? Map<*, *>
                    val lat = locationMap?.get("위도") as? Double
                    val lng = locationMap?.get("경도") as? Double

                    if (lat != null && lng != null) {
                        val place = Place(
                            name = document.getString("상호명") ?: "",
                            brand = document.getString("브랜드") ?: "",
                            address = document.getString("도로명주소") ?: "",
                            branch = document.getString("지점명") ?: "",
                            latLng = LatLng(lat, lng)
                        )
                        Log.d("MapViewModel", "Place 생성됨: $place")
                        place
                    } else {
                        Log.w("MapViewModel", "위도/경도 null: 제외됨")
                        null
                    }
                }
                _places.value = placeList // 먼저 장소 목록 업데이트
                calculateDistances() // 장소 목록 업데이트 후 거리 계산
            }
            .addOnFailureListener { e ->
                Log.e("MapViewModel", "Firebase fetch failed", e)
            }
    }

    // 현재 위치를 기반으로 각 장소까지의 거리를 계산하는 함수
    private fun calculateDistances() {
        val currentUserLocation = _userLocation.value
        if (currentUserLocation != null) {
            val updatedPlaces = _places.value.map { place ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    currentUserLocation.latitude,
                    currentUserLocation.longitude,
                    place.latLng.latitude,
                    place.latLng.longitude,
                    results
                )
                val distanceInMeters = results[0] // 거리는 미터 단위
                val distanceInKm = distanceInMeters / 1000.0 // 킬로미터로 변환

                // 새로운 Place 객체를 생성하여 distanceKm 필드만 업데이트
                place.copy(distanceKm = distanceInKm)
            }
            _places.value = updatedPlaces // 업데이트된 장소 목록으로 교체
            Log.d("MapViewModel", "거리 계산 완료: ${updatedPlaces.firstOrNull()?.distanceKm} km")
        } else {
            Log.d("MapViewModel", "사용자 위치를 알 수 없어 거리를 계산할 수 없습니다.")
        }
    }
}