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
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val pillDao = database.pillDao()

    val pills: LiveData<List<Pill>> = pillDao.getAllPills()

    fun addPill(name: String, dosage: String, count: Int, imageUri: String? = null) {
        viewModelScope.launch {
            pillDao.insertPill(
                Pill(
                    name = name,
                    dosage = dosage,
                    totalCount = count,
                    imageUri = imageUri
                )
            )
        }
    }

    fun deletePill(pill: Pill) {
        viewModelScope.launch {
            pillDao.deleteTakingsForPill(pill.id)
            pillDao.deletePill(pill)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                MainViewModel(application)
            }
        }
    }
}
