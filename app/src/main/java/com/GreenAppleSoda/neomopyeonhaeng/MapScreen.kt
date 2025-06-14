package com.GreenAppleSoda.neomopyeonhaeng

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.maps.android.compose.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    userLocation: LatLng?, // MainActivity에서 전달받을 userLocation 인자 추가
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val places = mapViewModel.places.value

    // ViewModel에 사용자 위치 업데이트
    mapViewModel.userLocation = userLocation

    val defaultLocation = LatLng(36.9803, 127.0462) // 팽성북로 기준
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: defaultLocation, 14f) // 사용자 위치가 있다면 중심으로 설정
    }

    Scaffold(
        topBar = {
            Column {
                OutlinedTextField(
                    value = "", // 실제 검색어 상태 연결 필요
                    onValueChange = { /* 검색어 변경 처리 */ },
                    label = { Text("지번, 도로명, 건물명으로 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Button(onClick = { /* 필터 액션 */ }) {
                        Text("Filter")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "필터")
                    }
                    Text("${places.size} results")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // topBar가 차지하는 공간만큼 패딩 적용
        ) {
            // 지도 영역 (남은 공간의 가중치로 할당)
            GoogleMap(
                modifier = Modifier
                    .weight(1f) // 남은 공간을 모두 차지하도록 설정
                    .fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                properties = com.google.maps.android.compose.MapProperties(isMyLocationEnabled = true),
                uiSettings = com.google.maps.android.compose.MapUiSettings(compassEnabled = true, myLocationButtonEnabled = true)
            ) {
                places.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.latLng),
                        title = place.name,
                        snippet = place.address
                    )
                }
            }

            // 장소 목록 영역
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // 높이 제한
            ) {
                items(places) { place ->
                    PlaceCard(place = place)
                }
            }
        }
    }
}


