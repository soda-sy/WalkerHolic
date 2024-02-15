# WalkerHolic_소모 칼로리만큼 식단 관리하자
---
## 앱 제작 동기
---
* 하루 동안 운동한 칼로리만큼 먹고 싶을 때, 몸무게를 기반으로 한 kcal 계산을 통해 맞춤형 식단을 제공
* 하루 동안의 활동량과 칼로리 소비 정보를 제공하여 건강한 식단 조절과 운동을 효과적으로 관리
---
## ENG) The motivation for developing an app
---
* When you want to eat as many calories as you burned during a day's exercise, we provide personalized diets based on kcal calculations according to your body weight
* We offer information on daily activity levels and calorie expenditure to effectively manage healthy diet adjustments and exercise
---

## 앱 기능
---
* 구글지도 활용 위치 수신
  
  > 소모 칼로리는 몸무게에 따라 계산이 달라진다
  > 걸음 수는 위치에 ‘걸음’을 기준으로 측정했으며 5로 나눈 나머지가 0일때 마다 추천 식단을 가져온다
  > 걸음수 계산
  ```
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
  ```
    ```
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
  ```
  ---
* 네이버API (image)활용
  
  > 엑셀파일에서 읽어온 음식 영양 정보를 다음과 같이 가져와서 adapter에 담아준다
  > 음식 이름에 해당하는 이미지는 네이버 API에서 획득
 ```
interface FoodImgAPIService {
    @GET("v1/search/image")
    fun getBooksByKeyword (
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") keyword: String,
        @Query("display") display: Int,
    )  : Call<Root>
}
  ```
  ```
fun readExcelByKcal(kcal: String): List<FoodItem> {
        val foodItemList = mutableListOf<FoodItem>()

        var inputStream = assets.open("FoodNutritionsDB_API.xlsx")
        var myWorkBook = WorkbookFactory.create(inputStream)

        try {
            val sheet = myWorkBook.getSheetAt(0)
            val rowIterator = sheet.iterator()

            while (rowIterator.hasNext()) {
                val myRow = rowIterator.next() as XSSFRow
                val foodItem = processRow(myRow, kcal)
                if (foodItem.calories > 0 && foodItem.calories >= kcal.toDouble() - 3
                    && foodItem.calories <= kcal.toDouble()) {
                    foodItemList.add(foodItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return foodItemList
    }
  ```
