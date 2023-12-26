package com.example.walkerholic

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.walkerholic.databinding.ActivityExerciseBinding
import com.example.walkerholic.manager.ExcelManager
import com.example.walkerholic.ui.FoodAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StyleSpan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ExerciseActivity : AppCompatActivity() {

    private val TAG = "MainActivityTag"

    val exerciseBinding by lazy {
        ActivityExerciseBinding.inflate(layoutInflater)
    }

    val adapter by lazy {
        FoodAdapter()
    }

    lateinit var excelManager: ExcelManager

    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geocoder : Geocoder
    private lateinit var currentLoc : Location
    var stepCount : Int = 0
    private lateinit var googleMap : GoogleMap
    var centerMarker : Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(exerciseBinding.root)
        excelManager = ExcelManager(assets)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        getLastLocation()   // 최종위치 확인

        checkPermissions()

        val pref: SharedPreferences = getSharedPreferences("my_info", 0)
        val editor: SharedPreferences.Editor = pref.edit()
        var todayDate = pref.getString("date", null)
        var kcal = pref.getString("kcal", null)
        var lastStepCount = pref.getString("stepCount", null)

        Log.d("ddd", "${todayDate} ${getCurrentTime()} ${kcal} ${lastStepCount}")
        if (getCurrentTime() != todayDate || todayDate == null) {
            editor.putString("date", getCurrentTime()).commit()
            todayDate = getCurrentTime()

            exerciseBinding.tvKcal.text = "0"
            exerciseBinding.tvStepCount.text = "0"
        }

        if (todayDate != null && getCurrentTime() == todayDate) {
            if(lastStepCount != null){
                stepCount = lastStepCount?.toInt() as Int
            } else{
                stepCount = 0
            }
            exerciseBinding.tvKcal.text = kcal
            exerciseBinding.tvStepCount.text = lastStepCount
        } else {
            exerciseBinding.tvKcal.text = "0"
            exerciseBinding.tvStepCount.text = "0"
        }

        exerciseBinding.rvRecommendFood.adapter = adapter
        exerciseBinding.rvRecommendFood.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : FoodAdapter.OnItemClickListner {
            override fun onItemClick(view: View, position: Int) {
                editor.putString("kcal", exerciseBinding.tvKcal.text.toString())
                editor.putString("stepCount",exerciseBinding.tvStepCount.text.toString()).commit()

                val selectedFood = adapter.foods?.get(position)
                val intent = Intent(applicationContext, WriteDiaryActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("food", selectedFood)
                intent.putExtra("todayDate", todayDate)
                intent.putExtra("stepCount", exerciseBinding.tvStepCount.text.toString())
                intent.putExtra("kcal", exerciseBinding.tvKcal.text.toString())
                startActivity(intent)
                finish()
            }
        })

        exerciseBinding.workOutToggle.setOnCheckedChangeListener( //CompundButton.OnCheckedChangedListener을 새로 선언
            CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
                // 첫번째 인자는 ToggleButton, 두번째 인자는 on/off에 대한 boolean값
                val toastMessage: String
                //toggle 버튼이 on된 경우

                if (isChecked) {
                    startLocUpdates()
                    addMarker(LatLng(37.606320, 127.041808))
                    //start할때 polyline liateinit 한거 초기화하기
                    polyLine = PolylineOptions()
                } else {
                    fusedLocationClient.removeLocationUpdates(locCallback)
                    centerMarker?.remove()
                    line.remove()

                    editor.putString("kcal", exerciseBinding.tvKcal.text.toString())
                    editor.putString("stepCount",exerciseBinding.tvStepCount.text.toString()).commit()
                }
            }
        )

//        exerciseBinding.btnLocTitle.setOnClickListener {
//            geocoder.getFromLocation(37.601025, 127.04153, 5) { addresses ->
//                CoroutineScope(Dispatchers.Main).launch {
//                    showData("위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
//                    showData(addresses.get(0).getAddressLine(0).toString())
//                }
//
//            }
//        }

        showData("Geocoder isEnabled: ${Geocoder.isPresent()}")

        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync (mapReadyCallback)

    }


    /*GoogleMap 로딩이 완료될 경우 실행하는 Callback*/
    val mapReadyCallback = object: OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is ready")
        }
    }


    /*마커 추가*/
    fun addMarker(targetLoc: LatLng) {  // LatLng(37.606320, 127.041808)
        val markerOptions : MarkerOptions = MarkerOptions()
        markerOptions.position(targetLoc)
            .title("마커 제목")
            .snippet("마커 말풍선")
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.android))

        centerMarker = googleMap.addMarker(markerOptions)
        centerMarker?.showInfoWindow()
        centerMarker?.tag = "database_id"
    }


    /*선 추가*/
    fun drawLine() {
        val polylineOptions = PolylineOptions()
            .addSpan(StyleSpan(Color.RED))
            .add(LatLng(37.604151, 127.042453))
            .add(LatLng(37.605347, 127.041207))
            .add(LatLng(37.606038, 127.041344))
            .add(LatLng(37.606220, 127.041674))
            .add( LatLng(37.606631, 127.041595))
            .add( LatLng(37.606823, 127.042380))

        val line = googleMap.addPolyline(polylineOptions)
    }

    private lateinit var polyLine: PolylineOptions
    private lateinit var line: Polyline
    private var previousLoc: Location? = null

    val locCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locResult: LocationResult) {
            currentLoc = locResult.locations[0]
            val targetLoc: LatLng = LatLng(currentLoc.latitude, currentLoc.longitude)
            centerMarker?.position = targetLoc
            Log.d(TAG, targetLoc.toString())
            polyLine.add(targetLoc)

            runOnUiThread {
                val pref : SharedPreferences = getSharedPreferences("my_info", 0)

                exerciseBinding.tvStepCount.text = "${stepCount}"
                if(!pref.getString("myWeight", null).equals(null)){
                    exerciseBinding.tvKcal.text = "${calculateKcalBurned(pref.getString("myWeight", null)!!.toInt())}"
                }

                if(stepCount % 5 == 0){
                    var foodItemList = excelManager.readExcelByKcal(exerciseBinding.tvKcal.text.toString())
                    adapter.foods = foodItemList
                    adapter.notifyDataSetChanged()
                }
            }

            if (previousLoc != null) {
                val distance = calculateStepCount(
                    previousLoc!!.latitude,
                    previousLoc!!.longitude,
                    currentLoc.latitude,
                    currentLoc.longitude
                )
                stepCount += distance.toInt()
            }

            if (::line.isInitialized) {
                line.remove()
            }

            line = googleMap.addPolyline(polyLine)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))

            previousLoc = currentLoc
        }
    }


    /*API 33 이전 사용 방식*/
//            CoroutineScope(Dispatchers.Main).launch {
//                val addresses = geocoder.getFromLocation(currentLoc.latitude, currentLoc.longitude, 5)
//                showData("위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
//                showData(addresses?.get(0)?.getAddressLine(0).toString())
//            }


    /*위치 정보 수신 설정*/
    val locRequest = LocationRequest.Builder(5000)
        .setMinUpdateIntervalMillis(3000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    /*위치 정보 수신 시작*/
    @SuppressLint("MissingPermission")
    private fun startLocUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,     // LocationRequest 객체
            locCallback,    // LocationCallback 객체
            Looper.getMainLooper()  // System 메시지 수신 Looper
        )
    }



    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

    /*LBSTest 관련*/
    //    최종위치 확인
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                showData(location.toString())
                currentLoc = location
            } else {
                currentLoc = Location("기본 위치")      // Last Location 이 null 경우 기본으로 설정
                currentLoc.latitude = 37.606816
                currentLoc.longitude = 127.042383
            }
        }
        fusedLocationClient.lastLocation.addOnFailureListener { e: Exception ->
            Log.d(TAG, e.toString())
        }
    }


    fun callExternalMap() {
        val locLatLng   // 위도/경도 정보로 지도 요청 시
                = String.format("geo:%f,%f?z=%d", 37.606320, 127.041808, 17)
        val locName     // 위치명으로 지도 요청 시
                = "https://www.google.co.kr/maps/place/" + "Hawolgok-dong"
        val route       // 출발-도착 정보 요청 시
                = String.format("https://www.google.co.kr/maps?saddr=%f,%f&daddr=%f,%f",
            37.606320, 127.041808, 37.601925, 127.041530)
        val uri = Uri.parse(locLatLng)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }


    private fun showData(data : String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }


    fun checkPermissions () {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            showData("Permissions are already granted")  // textView에 출력
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    //헤버사인 공식(위도,경도로 걸음 수 근사치 구함)
    fun calculateStepCount(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // 지구의 반지름 (미터 단위)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    fun calculateKcalBurned(weight : Int) : Int{
        var burnedKcal = (weight * stepCount * 0.75 * 0.0035).toInt()
        return burnedKcal
    }

    fun getCurrentTime() : String {
        return SimpleDateFormat("yyyy-MM-dd").format(Date())
    }

    /*registerForActivityResult 는 startActivityForResult() 대체*/
    val locationPermissionRequest
            = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) {
            permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                showData("FINE_LOCATION is granted")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                showData("COARSE_LOCATION is granted")
            }
            else -> {
                showData("Location permissions are required")
            }
        }
    }
}
