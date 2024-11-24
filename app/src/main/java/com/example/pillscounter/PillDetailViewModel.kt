package com.example.pillscounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pillscounter.data.AppDatabase
import com.example.pillscounter.data.Pill
import com.example.pillscounter.data.PillTaking
import kotlinx.coroutines.launch
import java.util.Date

class PillDetailViewModel(
    application: Application,
    private val pillId: Long
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val pillDao = database.pillDao()

    val pill: LiveData<Pill> = pillDao.getPillById(pillId)
    val takings: LiveData<List<PillTaking>> = pillDao.getTakingsForPill(pillId)

    fun addTaking(count: Int, timestamp: Date = Date()) {
        viewModelScope.launch {
            val currentPill = pill.value ?: return@launch
            if (count <= currentPill.totalCount) {
                pillDao.insertTaking(
                    PillTaking(
                        pillId = pillId,
                        timestamp = timestamp,
                        count = count
                    )
                )
                pillDao.updatePill(currentPill.copy(totalCount = currentPill.totalCount - count))
            }
        }
    }

    fun deleteTaking(taking: PillTaking) {
        viewModelScope.launch {
            val currentPill = pill.value ?: return@launch
            pillDao.deleteTaking(taking.id)
            // Restore the count when deleting a taking
            pillDao.updatePill(currentPill.copy(totalCount = currentPill.totalCount + taking.count))
        }
    }

    fun updateTaking(taking: PillTaking, newCount: Int) {
        viewModelScope.launch {
            val currentPill = pill.value ?: return@launch
            val countDifference = taking.count - newCount
            if (countDifference <= currentPill.totalCount) {
                pillDao.updateTaking(taking.copy(count = newCount))
                pillDao.updatePill(currentPill.copy(totalCount = currentPill.totalCount + countDifference))
            }
        }
    }

    companion object {
        fun Factory(pillId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                PillDetailViewModel(application, pillId)
            }
        }
    }
}
