package com.GreenAppleSoda.neomopyeonhaeng

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceCard(place: Place) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            // place.imageUrl 필드가 Place 데이터 클래스에 없으면 이 부분은 주석 처리하거나 제거하세요.
            // 현재 MapViewModel의 Place 정의에는 imageUrl이 없습니다.
            // AsyncImage(
            //     model = place.imageUrl,
            //     contentDescription = place.name,
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .height(150.dp),
            //     contentScale = ContentScale.Crop
            // )
            // Spacer(modifier = Modifier.height(8.dp)) // 이미지가 없으면 Spacer도 제거하거나 조정

            Text(text = place.name)
            // 평점, 리뷰 수 필드가 Place 데이터 클래스에 없으면 이 부분도 주석 처리하거나 제거하세요.
            // Text(text = "★ ${place.rating} (${place.reviewCount} reviews)")
            // 텍스트 내용에 이미지 없으면 제거
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = place.address) // 주소 추가
            Spacer(modifier = Modifier.height(4.dp))

            // distanceKm이 null이 아닐 때만 표시 (소수점 첫째 자리까지 표시)
            place.distanceKm?.let { distance ->
                Text(text = "📍 ${"%.1f".format(distance)} km") // km로 표시
            } ?: Text(text = "📍 거리 정보 없음") // 거리가 없을 때 표시
        }
    }
}