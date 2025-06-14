package com.GreenAppleSoda.neomopyeonhaeng

import com.google.android.gms.maps.model.LatLng

data class Place(
    val name: String = "",
    val brand: String = "",
    val address: String = "",
    val branch: String = "",
    val latLng: LatLng = LatLng(0.0, 0.0),
    val imageUrl: String = "",
    //val rating: Double = 0.0,
    //val reviewCount: Int = 0,
    val distanceKm: Double = 0.0
)
