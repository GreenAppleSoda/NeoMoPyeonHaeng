package com.GreenAppleSoda.neomopyeonhaeng

import androidx.annotation.DrawableRes

object StoreLogoResources {
    @DrawableRes
    fun getStoreLogoResId(storeName: String): Int {
        return when (storeName) {
            "GS25" -> R.drawable.gs25_logo
            "CU" -> R.drawable.cu_logo
            "emart24" -> R.drawable.emart24_logo // Firestore의 store 필드와 정확히 일치하는 문자열 사용
            "7-ELEVEN" -> R.drawable.seven_eleven_logo
            else -> R.drawable.nmph_logo2 // 기본값 (예: 앱 로고, 빈 이미지 등)
        }
    }
}