package com.GreenAppleSoda.neomopyeonhaeng

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// LazyListScope의 확장 함수로 gridItems를 정의합니다.
// 이렇게 하면 MainScreen의 LazyColumn 스코프 안에서 직접 호출할 수 있습니다.
fun LazyListScope.productGridItems(
    productList: List<Product>,
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val rows = productList.chunked(columns) // 아이템을 열 개수만큼 묶어서 행으로 만듭니다.

    rows.forEachIndexed { rowIndex, row ->
        item {
            Column(
                modifier = modifier.fillMaxWidth()
                    .padding(horizontal = contentPadding.calculateLeftPadding(LayoutDirection.Ltr))
                    .padding(horizontal = contentPadding.calculateRightPadding(LayoutDirection.Ltr))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    row.forEachIndexed { itemIndex, product ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductItem(
                                imageUrl = product.image_url,
                                event_type = product.event_type,
                                name = product.name,
                                price = product.price,
                                store = product.store
                            )
                        }
                        if (itemIndex < row.lastIndex) {
                            Spacer(Modifier.width(horizontalArrangement.spacing))
                        }
                    }
                    // 빈 공간 채우기 (마지막 행이 열 개수에 못 미칠 경우)
                    for (i in 0 until (columns - row.size)) {
                        Spacer(Modifier.weight(1f))
                        if (i < (columns - row.size) - 1) {
                            Spacer(Modifier.width(horizontalArrangement.spacing))
                        }
                    }
                }
                if (rowIndex < rows.lastIndex) {
                    Spacer(Modifier.height(verticalArrangement.spacing))
                }
            }
        }
    }
}

@Composable
fun ProductItem(imageUrl: String, event_type: String, name: String, price: String, store: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth() // Grid 셀에 맞게 너비 조정
            .background(Color.White, RoundedCornerShape(12.dp)) // 배경과 둥근 모서리
            .padding(8.dp) // 내부 패딩
    ) {
        // 상품 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // 스토어 이름
        Text(
            text = store,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        // 행사 유형
        Text(
            text = event_type,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        // 상품 이름
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        // 상품 가격
        Text(
            text = price,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}