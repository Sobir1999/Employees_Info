package me.ruyeo.employeesinfo.view

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.flow.collect
import me.ruyeo.employeesinfo.R
import me.ruyeo.employeesinfo.data.api.ApiClient
import me.ruyeo.employeesinfo.data.api.ApiHelper
import me.ruyeo.employeesinfo.data.api.ApiService
import me.ruyeo.employeesinfo.data.local.AppDatabase
import me.ruyeo.employeesinfo.data.model.TokenManager
import me.ruyeo.employeesinfo.databinding.FragmentHomeBinding
import me.ruyeo.employeesinfo.repository.factory.HomeViewModelFactory
import me.ruyeo.employeesinfo.utils.Utils
import me.ruyeo.employeesinfo.utils.extensions.viewBinding
import me.ruyeo.employeesinfo.viewModel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding { FragmentHomeBinding.bind(it) }
    private val navController by lazy { Navigation.findNavController(binding.root) }
    private val viewModel by lazy { setupViewModel() }
    private val progressDialog by lazy { setupDialog() }
    private lateinit var dateNow: String
    private val appDatabase by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
//                val dialogBuilder = AlertDialog.Builder(requireContext())
//                dialogBuilder.setMessage("")
//                    .setCancelable(false)
//                    .setPositiveButton("Proceed", DialogInterface.OnClickListener { dialog, id ->
//                        finish()
//                    })
//                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
//                        dialog.cancel()
//                    })
//
//                // create dialog box
//                val alert = dialogBuilder.create()
//                // set title for alert dialog box
//                alert.setTitle("AlertDialogExample")
//                // show alert dialog
//                alert.show()
                activity?.finish()
            }
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupGetStaffObservers()

        if (!checkPermission()) {
            requestPermission()
        }

    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    private fun setupUI() {
        getDate()
        val someHandler = Handler(getMainLooper())
        someHandler.postDelayed(object : Runnable {
            override fun run() {
                binding.idCurrentTime.text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                binding.idCurrentDate.text = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
                someHandler.postDelayed(this, 1000)
            }
        }, 10)
        binding.apply {
//            idCurrentLocation.text = dateNow
            idComeToWork.setOnClickListener { navigateToScanningFrag(1) }
            idWentFromWork.setOnClickListener { navigateToScanningFrag(2) }
            idGoToLunch.setOnClickListener { navigateToScanningFrag(3) }
            idBackFromLaunch.setOnClickListener { navigateToScanningFrag(4) }
            idExit.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setMessage("Are you sure to Log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", { dialog, id ->
                        TokenManager.getInstance(
                            requireActivity().getSharedPreferences(
                                "prefs",
                                Context.MODE_PRIVATE
                            )
                        ).also {
                            it?.deleteToken()
                            navController.navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
                        }
                    })
                    .setNegativeButton("Cancel", { dialog, id ->
                        dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.setTitle("Logging out")
                // show alert dialog
                alert.show()

            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED){
            viewModel.homeState.collect {
                when (it) {
                    is HomeViewModel.HomeUiState.LOADING -> {
                       progressDialog.show()
                    }
                    is HomeViewModel.HomeUiState.SUCCESS -> {
                        progressDialog.dismiss()
                        Utils.toast(requireContext(), "Tizimdan chiqdinggiz")
                    }
                    is HomeViewModel.HomeUiState.ERROR -> {
                        progressDialog.dismiss()
                        Utils.toast(requireContext(), it.message)
                    }
                    else -> Unit
                }
            }
            }
        }
    }

    private fun getDate(){
        val mcurrentTime = Calendar.getInstance()
        val month = mcurrentTime.get(Calendar.MONTH) + 1

        dateNow = "${mcurrentTime.get(Calendar.YEAR)}-${
            String.format(
                "%02d",
                month
            )
        }-${mcurrentTime.get(Calendar.DAY_OF_MONTH)} ${mcurrentTime.get(Calendar.HOUR_OF_DAY)}:${
            mcurrentTime.get(
                String.format("%02d", Calendar.MINUTE).toInt()
            )
        }"
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun setupGetStaffObservers() {
        viewModel.getAllStaff()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED){
            viewModel.staffState.collect {
                when(it){
                    is HomeViewModel.StaffUiState.LOADING ->{
                        progressDialog.show()
                    }
                    is HomeViewModel.StaffUiState.SUCCESS ->{
                        progressDialog.dismiss()
                        it.data.forEach {
//                            binding.filial.text = it.company
                        }
                    }
                    is HomeViewModel.StaffUiState.ERROR -> {
                        progressDialog.dismiss()
                        Utils.toast(requireContext(),it.message)
                    }
                    else -> Unit
                }
            }
            }
        }
    }

    private fun setupViewModel() = ViewModelProvider(
        this, HomeViewModelFactory(
            ApiHelper(
                ApiClient.createServiceWithAuth(ApiService::class.java, requireContext())
            ),appDatabase.getStaffDao()
        )
    ).get(HomeViewModel::class.java)

    private fun setupDialog() = SpotsDialog.Builder()
        .setContext(requireContext())
        .setMessage("Yuklanmoqda")
        .setCancelable(false)
        .build()

    private fun navigateToScanningFrag(isCome: Int) {
        val intent = Intent(requireActivity(), ScanningActivity::class.java)
        intent.putExtra("come", isCome)
        startActivity(intent)
//        navController.navigate(HomeFragmentDirections.actionHomeFragmentToScanningFragment(isCome))
    }

}