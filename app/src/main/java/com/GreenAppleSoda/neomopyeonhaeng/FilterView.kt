package com.GreenAppleSoda.neomopyeonhaeng

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterView(productViewModel: ProductViewModel) {
    // FilterView 내부에서 상태 관리, ProductViewModel에 상태 통합
    //var selectedFilter by remember { mutableStateOf("행사 전체") } // 초기값 설정 (예: "행사 전체")
    val selectedFilter by productViewModel.selectedEvent.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        RecommendationTitle(selectedFilter = selectedFilter)
        FilterOptions(
            selectedFilter = selectedFilter,
            onFilterSelected = { filterText ->
                productViewModel.selectEvent(filterText)
            }
        )
    }
}

@Composable
private fun RecommendationTitle(selectedFilter: String) {
    // "행사 전체"가 선택되었거나 아무것도 선택되지 않았을 때 "전체 상품 목록"으로 표시
    val titleText = when (selectedFilter) {
        "", "행사 전체" -> "전체 상품 목록"
        else -> "${selectedFilter} 상품 목록"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = titleText, // 동적으로 변경되는 텍스트
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Icon(
            Icons.Outlined.GridView,
            contentDescription = "보기 형식"
        )
    }
}

@Composable
private fun FilterOptions(
    selectedFilter: String, // 현재 선택된 필터 텍스트
    onFilterSelected: (String) -> Unit // 필터 선택 시 호출될 함수
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = "행사 전체",
            isSelected = selectedFilter == "행사 전체",
            onClick = { onFilterSelected("행사 전체") }
        )
        FilterChip(
            text = "1+1",
            isSelected = selectedFilter == "1+1",
            onClick = { onFilterSelected("1+1") }
        )
        FilterChip(
            text = "2+1",
            isSelected = selectedFilter == "2+1",
            onClick = { onFilterSelected("2+1") }
        )
        FilterChip(
            text = "덤 증정",
            isSelected = selectedFilter == "덤 증정",
            onClick = { onFilterSelected("덤 증정") }
        )
        FilterChip(
            text = "할인",
            isSelected = selectedFilter == "할인",
            onClick = { onFilterSelected("할인") }
        )
        FilterChip(
            text = "골라담기",
            isSelected = selectedFilter == "골라담기",
            onClick = { onFilterSelected("골라담기") }
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean, // 이 칩이 선택되었는지 여부
    onClick: () -> Unit // 칩 클릭 시 호출될 함수
) {
    // 칩이 선택되었을 때의 색상 정의
    val backgroundColor = if (isSelected) Color(0xFF3FB958) else Color.White // 선택 시 파란색 계열, 미선택 시 흰색
    val borderColor = if (isSelected) Color(0xFF3FB958) else Color.LightGray // 선택 시 파란색 계열, 미선택 시 연한 회색
    val textColor = if (isSelected) Color.White else Color.DarkGray // 선택 시 흰색, 미선택 시 어두운 회색

    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)) // 테두리 색상 변경
            .background(backgroundColor) // 배경 색상 변경
            .clickable(onClick = onClick) // 클릭 가능하게 만듦
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor // 텍스트 색상 변경
        )
    }
}