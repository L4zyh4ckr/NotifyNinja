
package dev.notifyninja.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dev.notifyninja.R
import dev.notifyninja.reminders.Frequency
import dev.notifyninja.reminders.Reminder
import dev.notifyninja.reminders.ReminderViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ManagerActivity : ComponentActivity() {
    private val vm: ReminderViewModel by viewModels()
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker
    private var selectedCategory = "Other"
    private var selectedFrequency = Frequency.NONE
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        
        // Setup category chips
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.categoryChipGroup)
        val categories = listOf("Payments", "Classes", "Journeys", "Exercise", "Other")
        val categoryColors = listOf(
            R.color.category_payments,
            R.color.category_classes,
            R.color.category_journeys,
            R.color.category_exercise,
            R.color.category_other
        )
        
        categories.forEachIndexed { index, category ->
            val chip = Chip(this)
            chip.text = category
            chip.isCheckable = true
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, categoryColors[index])
            )
            chip.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onPrimary))
            
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedCategory = category
                }
            }
            
            chipGroup.addView(chip)
            if (category == "Other") {
                chip.isChecked = true
            }
        }
        
        // Setup date picker
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
            
        // Setup time picker
        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(getString(R.string.time))
            .build()
            
        // Setup frequency selection
        val radioGroup = findViewById<android.widget.RadioGroup>(R.id.frequencyRadioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedFrequency = when(checkedId) {
                R.id.freqDaily -> Frequency.DAILY
                R.id.freqWeekly -> Frequency.WEEKLY
                R.id.freqMonthly -> Frequency.MONTHLY
                else -> Frequency.NONE
            }
        }
        
        // Setup save button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.saveBtn).setOnClickListener {
            val titleInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleInput)
            val title = titleInput.text.toString().ifBlank { "Reminder" }
            
            val cal = Calendar.getInstance().apply {
                timeInMillis = datePicker.selection ?: System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val reminder = Reminder(
                id = 0,
                title = title,
                category = selectedCategory,
                triggerAt = cal.timeInMillis,
                frequency = selectedFrequency
            )
            
            lifecycleScope.launch {
                vm.save(reminder)
                Toast.makeText(this@ManagerActivity, "Reminder saved and scheduled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
