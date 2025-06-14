package com.GreenAppleSoda.neomopyeonhaeng

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Preview(showBackground = true, showSystemUi = true)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerView() {
    val bannerList = remember {
        listOf(
            R.drawable.gs25_banner,
            R.drawable.cu_banner,
            R.drawable.emart24_banner,
            R.drawable._7eleven_banner
        )
    }

    // ✨✨✨ 각 배너 이미지에 연결할 URL 리스트를 정의합니다. ✨✨✨
    val bannerUrls = remember {
        listOf(
            "http://gs25.gsretail.com/gscvs/ko/customer-engagement/event/current-events",
            "https://cu.bgfretail.com/brand_info/news_list.do?category=brand_info&depth2=5&sf=N",
            "https://emart24.co.kr/event/ing",
            "https://m.7-eleven.co.kr/product/eventList.asp"
        )
    }

    val pagerState = rememberPagerState(pageCount = { bannerList.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 자동 슬라이드 효과 (선택 사항)
    LaunchedEffect(key1 = pagerState) {
        while(true) {
            delay(3000) // 3초마다 슬라이드 전환
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % bannerList.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(140.dp)
//            .clip(RoundedCornerShape(
//                topStart = 16.dp,  // 왼쪽 상단 16dp 둥글게
//                topEnd = 16.dp,    // 오른쪽 상단 16dp 둥글게
//                bottomStart = 0.dp, // 하단은 0dp로 직각 유지
//                bottomEnd = 0.dp
//            ))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageRes = bannerList[page]
            val targetUrl = bannerUrls[page] // ✨ 현재 페이지의 URL을 가져옵니다. ✨

            // 페이지 애니메이션 효과 (선택 사항)
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                    ).absoluteValue

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .graphicsLayer {
                        // 살짝 줌 아웃/인 효과
                        val scale = 0.9f + 0.1f * (1f - pageOffset.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale

                        // 약간의 투명도 변화
                        alpha = 0.5f + 0.5f * (1f - pageOffset.coerceIn(0f, 1f))
                    }
                    // ✨✨✨ 클릭 가능한 영역을 추가합니다. ✨✨✨
                    .clickable {
                        // 클릭 시 해당 URL을 웹 브라우저로 엽니다.
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        context.startActivity(intent)
                    }
            ) {
                // 배너 이미지 전체를 차지하도록 수정
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "배너 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 페이지 인디케이터
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(bannerList.size) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index)
                                Color.Black
                            else
                                Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}
