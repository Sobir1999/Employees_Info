package me.ruyeo.employeesinfo.repository.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.ruyeo.employeesinfo.data.api.ApiHelper
import me.ruyeo.employeesinfo.data.local.AppDatabase
import me.ruyeo.employeesinfo.repository.ScanningRepository
import me.ruyeo.employeesinfo.viewModel.ScanningViewModel

/**
 *Created by farrukh_kh on 6/12/21 6:48 PM
 *me.ruyeo.employeesinfo.repository.factory
 **/
class ScanningViewModelFactory(
    private val apiHelper: ApiHelper,
    private val appDatabase: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanningViewModel::class.java)) {
            return ScanningViewModel(ScanningRepository(apiHelper, appDatabase)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}