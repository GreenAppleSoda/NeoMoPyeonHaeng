package com.GreenAppleSoda.neomopyeonhaeng

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        BottomBarItem("홈", Icons.Outlined.Home),
        BottomBarItem("게시판", Icons.Outlined.Forum),
        BottomBarItem("스캔", null), // 가운데 버튼은 별도 처리
        BottomBarItem("지도", Icons.Outlined.LocationOn),
        BottomBarItem("내 정보", Icons.Outlined.Person)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp), // 여백 조금 줄임
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) {
                    Spacer(modifier = Modifier.weight(1f)) // 가운데 비우기
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f) // 아이템 하나가 Row의 1/5 차지
                            .fillMaxHeight()
                            .clickable { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        BottomBarTab(
                            label = item.label,
                            icon = item.icon!!,
                            selected = selectedIndex == index,
                            onClick = { onItemSelected(index) }
                        )
                    }
                }
            }
        }


        // 가운데 버튼 따로 배치
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6F00))
                .clickable { onItemSelected(2) },
            contentAlignment = Alignment.Center
        ) {
            Text("스캔", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun BottomBarTab(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}


data class BottomBarItem(val label: String, val icon: ImageVector?)