package com.example.walkerholic

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.walkerholic.data.DiaryRecord
import com.example.walkerholic.data.DiaryRecordDao
import com.example.walkerholic.data.DiaryRecordDatabase
import com.example.walkerholic.data.FoodItem
import com.example.walkerholic.databinding.ActivityDiaryBinding
import com.example.walkerholic.databinding.ActivityWriteDiaryBinding
import com.example.walkerholic.ui.DiaryAdapter
import com.example.walkerholic.ui.FoodAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class DiaryActivity : AppCompatActivity() {

    val diaryBinding by lazy {
        ActivityDiaryBinding.inflate(layoutInflater)
    }

    lateinit var adapter : DiaryAdapter
    lateinit var db : DiaryRecordDatabase
    lateinit var diaryRecordDao : DiaryRecordDao

    var recordList = ArrayList<DiaryRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(diaryBinding.root)
        diaryBinding.calendarView.setSelectedDate(CalendarDay.today())
        diaryBinding.calendarView.addDecorators(TodayDecorator(), SaturdayDecorator())

        db = DiaryRecordDatabase.getDatabase(this)
        diaryRecordDao = db.diaryRecordDao()

        getAllRecords()

        adapter = DiaryAdapter()
        diaryBinding.rvRecord.adapter = adapter

        adapter.setOnItemClickListener(object : DiaryAdapter.OnItemClickListner {
            override fun onItemClick(view: View, position: Int) {
                val selectedRecord = adapter.diaryRecords?.get(position)
                val intent = Intent(applicationContext, EditDiaryActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("record", selectedRecord)
                startActivity(intent)
                finish()
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            // 데이터 로딩이 완료되었을 때의 로직
            withContext(Dispatchers.Main) {
                val tempList = onMonthChanged(
                    diaryBinding.calendarView.currentDate.year.toString(),
                    (diaryBinding.calendarView.currentDate.month + 1).toString(),
                    recordList
                )

                diaryBinding.rvRecord.layoutManager = LinearLayoutManager(this@DiaryActivity).apply {
                    orientation = LinearLayoutManager.VERTICAL
                }
                diaryBinding.rvRecord.adapter = adapter

                adapter.updateList(tempList)
            }
        }

        diaryBinding.calendarView.setOnMonthChangedListener { widget, date ->
            val TempList = onMonthChanged(date.year.toString(), (date.month + 1).toString(), recordList)
            adapter.updateList(TempList)
        }

    }
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    fun onMonthChanged(year : String, month : String, recordList : ArrayList<DiaryRecord>) : ArrayList<DiaryRecord>{
        val pageYearMonth = "${year}-${month}"
        val TempList = ArrayList<DiaryRecord>()
        val calendarDays = mutableSetOf<CalendarDay>()

        for(record in recordList){
            val eventParts = record.date.split("-")
            if (eventParts.size >= 3) {
                val eventYear = eventParts[0].toInt()
                val eventMonth = eventParts[1].toInt()
                val eventDay = eventParts[2].toInt()
                calendarDays.add(CalendarDay.from(eventYear, (eventMonth - 1), eventDay))
            }
            val eventYearMonth = record.date?.substringBeforeLast("-")
            if(eventYearMonth == pageYearMonth){
                diaryBinding.calendarView.addDecorator(EventDecorator(calendarDays))
                TempList.add(record)
            }
        }
        return TempList
    }

    class TodayDecorator: DayViewDecorator {
        private var date = CalendarDay.today()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return day?.equals(date)!!
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(StyleSpan(Typeface.BOLD))
            view?.addSpan(RelativeSizeSpan(1.4f))
            view?.addSpan(ForegroundColorSpan(Color.parseColor("#3F51B5")))
        }
    }
    class EventDecorator(dates: Collection<CalendarDay>): DayViewDecorator {

        var dates: HashSet<CalendarDay> = HashSet(dates)

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(DotSpan(8F, Color.parseColor("#FF0000")))
        }
    }

    class SaturdayDecorator: DayViewDecorator {

        private val calendar = Calendar.getInstance()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            day?.copyTo(calendar)
            val saturday = calendar.get(Calendar.DAY_OF_WEEK)
            return saturday == Calendar.SATURDAY
        }
        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(object: ForegroundColorSpan(Color.RED){})
        }
    }

    fun getAllRecords() {
        CoroutineScope(Dispatchers.IO).launch {
            val recordflow = diaryRecordDao.getAllRecords()

            //distinct 사용해서 수정, 추가, 삭제등 변경 없을 시 수행 x
            recordflow.distinctUntilChanged().collect{records ->
                for(record in records){
                    Log.d("ddd", "${record.foodName}")
                }
                    recordList.addAll(records)
            }
        }
    }
}