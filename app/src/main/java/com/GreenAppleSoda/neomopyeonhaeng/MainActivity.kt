package com.GreenAppleSoda.neomopyeonhaeng

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.GreenAppleSoda.neomopyeonhaeng.ui.theme.NeoMoPyeonHaengTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 현재 위치 상태를 Compose에서 관찰할 수 있도록
    private var currentLocation: LatLng? by mutableStateOf(null)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Log.d("MainActivity", "위치 권한 승인됨")
                fetchLastLocation() // 권한 승인 후 바로 위치 가져오기 시도
            } else {
                Log.w("MainActivity", "위치 권한 거부됨")
                // 사용자에게 권한이 필요한 이유를 설명하거나, 위치 기반 기능 비활성화
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Firebase 초기화

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContent {
            NeoMoPyeonHaengTheme {
                val navController = rememberNavController()

                // ✨ ProductViewModel을 여기서 한번만 생성 ✨
                val db = FirebaseFirestore.getInstance()
                val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(db))

                // 3. 현재 라우트를 관찰하여 BottomBar 선택 상태 업데이트
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                //var selectedIndex by remember { mutableStateOf(0) }

                // 권한 요청 로직
                DisposableEffect(Unit) {
                    // 앱 시작 시 권한 상태 확인 및 요청
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        // 이미 권한이 있다면 위치 가져오기 시도
                        fetchLastLocation()
                    }
                    onDispose { /* Cleanup, if needed */ }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // 5. CustomBottomBar 통합 및 내비게이션 로직 추가
                        // 현재 라우트에 따라 selectedIndex를 결정 (바텀바 하이라이트)
                        val selectedIndex = when (currentRoute) {
                            NavRoutes.HOME -> 0
                            NavRoutes.BOARD -> 1
                            NavRoutes.SCAN -> 2
                            NavRoutes.MAP -> 3
                            NavRoutes.MY_INFO -> 4
                            else -> 0 // 기본값은 홈
                        }

                        CustomBottomBar(
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                val routeToNavigate = when (index) {
                                    0 -> NavRoutes.HOME
                                    1 -> NavRoutes.BOARD
                                    2 -> NavRoutes.SCAN
                                    3 -> NavRoutes.MAP
                                    4 -> NavRoutes.MY_INFO
                                    else -> NavRoutes.HOME
                                }
                                navController.navigate(routeToNavigate) {
                                    // 탭 전환 시 백스택 관리:
                                    // 시작 대상으로 팝업하여 스택 정리, 상태 저장
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true // 동일 대상 중복 생성 방지
                                    restoreState = true // 이전 상태 복원
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME, // 앱 시작 시 첫 화면
                        modifier = Modifier.padding(innerPadding) // Scaffold의 패딩 적용
                    ) {
                        // 각 화면 정의
                        composable(NavRoutes.HOME) {
                            // MainScreen에 NavController 전달
                            MainScreen(
                                productViewModel = productViewModel,
                                navController = navController
                            )
                        }
                        composable(NavRoutes.SEARCH) {
                            SearchScreen(navController = navController)
                        }
//                        composable(NavRoutes.BOARD) {
//                            BoardScreen(navController = navController)
//                        }
//                        composable(NavRoutes.SCAN) {
//                            ScanScreen(navController = navController)
//                        }
                        composable(NavRoutes.MAP) {
                            MapScreen(navController = navController, userLocation = currentLocation)
                        }
//                        composable(NavRoutes.MY_INFO) {
//                            MyInfoScreen(navController = navController)
//                        }
//                        composable(NavRoutes.PRODUCT_DETAIL) { backStackEntry ->
//                            val productId = backStackEntry.arguments?.getString("productId")
//                            ProductDetailScreen(
//                                navController = navController,
//                                productId = productId
//                            )
//                        }
                    }
                }
            }
        }
    }

    // 마지막으로 알려진 위치를 가져오는 함수
    private fun fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        Log.d("MainActivity", "현재 위치: ${currentLocation?.latitude}, ${currentLocation?.longitude}")
                    }
                    else {
                        Log.w("MainActivity", "마지막 위치를 찾을 수 없음")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "위치 가져오기 실패", e)
                }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel,
    navController: NavController
) {
    val productList by productViewModel.filteredProductList.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState() // <--- 이 부분 수정
    val errorMessage by productViewModel.errorMessage.collectAsState() // <--- 이 부분 수정

    Column(modifier = modifier.fillMaxSize()) {
        val startColor = Color(0xFFBEF5CA)
        val endColor = Color(0xFF3FB958)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to startColor, // 시작 지점(0%)부터 startColor
                            0.7f to startColor, // 80% 지점까지 startColor 유지 (거의 변함 없음)
                            1.0f to endColor    // 100% 지점에서 endColor
                        ),
                        start = Offset(0f, 0f), // 상단 시작
                        end = Offset(0f, Float.POSITIVE_INFINITY) // 하단 끝 (수직 그라데이션)
                    )
                )
        ) {
            // TopAppBar에 ViewModel과 selectStore 람다 전달
            val selectedStore by productViewModel.selectedStore.collectAsState() // 현재 선택된 편의점 상태도 관찰

            TopAppBar(
                selectedStore = selectedStore,
                onStoreSelected = { storeName ->
                    // ✨ 이 로그는 AlertDialog에서 올라온 콜백이 TopAppBar까지 잘 왔는지 확인 ✨
                    Log.d("ProductDebug", "TopAppBar: onStoreSelected received from dialog: $storeName")
                    productViewModel.selectStore(storeName)
                },
                allStoreNames = productViewModel.getAllStoreNames() // 모든 편의점 이름 전달
            )
            FakeSearchBar {
                navController.navigate(NavRoutes.SEARCH) {
                    popUpTo(navController.graph.startDestinationId) { // 시작 대상까지 팝업
                        saveState = true
                    }
                    launchSingleTop = true // 동일 대상 중복 생성 방지
                    restoreState = true // 이전 상태 복원
                }
            }
        }
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(
                    topStart = 16.dp,  // 왼쪽 상단 16dp 둥글게
                    topEnd = 16.dp,    // 오른쪽 상단 16dp 둥글게
                    bottomStart = 0.dp, // 하단은 0dp로 직각 유지
                    bottomEnd = 0.dp
                ))
                .background(Color.LightGray)
        ) {
            item {
                Column {
                    BannerView()
                    FilterView(productViewModel = productViewModel)
                }
            }

            // 로딩, 에러, 빈 상태 처리
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Text("상품 정보를 불러오는 중...", modifier = Modifier.padding(top = 50.dp))
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(errorMessage!!, color = Color.Red)
                    }
                }
            } else if (productList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("표시할 상품이 없습니다.")
                    }
                }
            } else {
                // productGridItems 확장 함수를 호출하여 상품 목록을 그리드 형태로 표시
                // ProductGrid 컴포저블을 직접 사용하지 않고, 그 내부 로직을 LazyColumn의 item으로 삽입합니다.
                productGridItems(
                    productList = productList,
                    columns = 2,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp)
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    selectedStore: String, // 현재 선택된 편의점 이름
    onStoreSelected: (String) -> Unit, // 편의점 선택 시 호출될 콜백
    allStoreNames: List<String> // 모든 편의점 이름 목록
) {
    var showDialog by rememberSaveable { mutableStateOf(false) } // 다이얼로그 표시 여부

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { showDialog = true }, // 버튼 클릭 시 다이얼로그 표시
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(Color.Transparent)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedStore == "편의점 선택" || selectedStore == "전체") {
                    Text(
                        color = Color.Black,
                        text = selectedStore,
                        fontSize = 18.sp, // 텍스트 크기 조절
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Image(
                        painter = painterResource(id = StoreLogoResources.getStoreLogoResId(selectedStore)),
                        contentDescription = "$selectedStore Logo",
                        modifier = Modifier.height(40.dp), // 로고 높이 고정
                        contentScale = ContentScale.Fit // 이미지 비율 유지
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.ExpandCircleDown,
                    tint = Color.Black,
                    contentDescription = "편의점 목록",
                )
            }
        }
        Row {
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "알림")
            }
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = "찜 목록")
            }
        }
    }

    // 편의점 선택 다이얼로그
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // 다이얼로그 바깥 클릭 시 닫기
            title = { Text("편의점 선택") },
            text = {
                Column {
                    allStoreNames.forEach { storeName ->
                        TextButton(
                            onClick = {
                                // ✨ 이 부분이 중요합니다 ✨
                                Log.d("ProductDebug", "AlertDialog: selected $storeName") // 다이얼로그에서 선택 시 로그
                                onStoreSelected(storeName) // ViewModel에 선택된 편의점 전달
                                showDialog = false // 다이얼로그 닫기
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(storeName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun FakeSearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(24.dp))
            .clickable { onClick() } // 클릭 시 동작
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "아 그거 행사하나?",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
        }
    }
}

//@Composable
//fun FullAppPreviewWrapper() {
//    // 1. FirebaseFirestore 더미 객체 생성 (미리보기 전용)
//    // 실제 Firebase 연결 없이 미리보기에서만 사용할 가짜 인스턴스입니다.
//    // 주의: 이 db 객체는 실제 Firestore 기능을 수행하지 않습니다.
//    // 단지 ProductViewModelFactory가 db 인자를 요구하기 때문에 제공하는 것입니다.
//    val mockFirestoreDb = FirebaseFirestore.getInstance() // 미리보기에서는 이 호출이 실제 DB에 연결되지 않습니다.
//
//    // 2. ProductViewModel 인스턴스 생성 (미리보기 전용)
//    // viewModel() 함수는 @Composable 컨텍스트에서만 호출 가능합니다.
//    // 또한, 미리보기 환경에서 실제 ViewModel을 사용하려면 ViewModelProvider.Factory가 필요합니다.
//    // 여기서는 @Composable 컨텍스트에서 viewModel()을 호출하고,
//    // 실제 DB 접근을 하지 않는 ProductViewModelFactory를 전달합니다.
//    val previewProductViewModel: ProductViewModel = viewModel(
//        factory = ProductViewModelFactory(mockFirestoreDb)
//    )
//    NeoMoPyeonHaengTheme {
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//            topBar = {
//                Column {
//                    // TopAppBar에 ViewModel과 selectStore 람다 전달
//                    val db = FirebaseFirestore.getInstance()
//                    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(db))
//                    val selectedStore by productViewModel.selectedStore.collectAsState() // 현재 선택된 편의점 상태도 관찰
//
//                    TopAppBar(
//                        selectedStore = selectedStore,
//                        onStoreSelected = { storeName ->
//                            productViewModel.selectStore(storeName)
//                        },
//                        allStoreNames = productViewModel.getAllStoreNames() // 모든 편의점 이름 전달
//                    )
//                    ProductSearchBar()
//                }
//            }
//        ) { innerPadding ->
//            MainScreen(modifier = Modifier.padding(innerPadding), productViewModel = previewProductViewModel)
//        }
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun FullAppPreview() {
//    FullAppPreviewWrapper()
//}