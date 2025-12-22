package com.inspection.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.inspection.app.R
import com.inspection.app.databinding.ActivityMainBinding
import com.inspection.app.ui.cases.CaseListFragment
import com.inspection.app.ui.addresses.AddressListFragment
import com.inspection.app.ui.photos.PhotoListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentScreen = Screen.CASES
    private var currentCaseId: Long = -1
    private var currentAddressId: Long = -1
    private var currentAddressName: String = ""

    enum class Screen {
        CASES, ADDRESSES, PHOTOS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        showCaseList()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun showCaseList() {
        currentScreen = Screen.CASES
        binding.toolbar.title = getString(R.string.app_name)
        binding.toolbar.navigationIcon = null
        binding.fab.setImageResource(android.R.drawable.ic_input_add)
        binding.fab.visibility = View.VISIBLE

        replaceFragment(CaseListFragment())

        binding.fab.setOnClickListener {
            (supportFragmentManager.findFragmentById(R.id.container) as? CaseListFragment)?.showAddCaseDialog()
        }
    }

    fun showAddressList(caseId: Long, caseName: String) {
        currentScreen = Screen.ADDRESSES
        currentCaseId = caseId
        binding.toolbar.title = caseName
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.fab.setImageResource(android.R.drawable.ic_input_add)
        binding.fab.visibility = View.VISIBLE

        replaceFragment(AddressListFragment.newInstance(caseId))

        binding.fab.setOnClickListener {
            (supportFragmentManager.findFragmentById(R.id.container) as? AddressListFragment)?.showAddAddressDialog()
        }
    }

    fun showPhotoList(addressId: Long, addressName: String) {
        currentScreen = Screen.PHOTOS
        currentAddressId = addressId
        currentAddressName = addressName
        binding.toolbar.title = addressName
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.fab.setImageResource(R.drawable.ic_camera)
        binding.fab.visibility = View.VISIBLE

        replaceFragment(PhotoListFragment.newInstance(addressId, addressName))

        binding.fab.setOnClickListener {
            (supportFragmentManager.findFragmentById(R.id.container) as? PhotoListFragment)?.takePhoto()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.container, fragment)
        }
    }

    override fun onBackPressed() {
        when (currentScreen) {
            Screen.PHOTOS -> showAddressList(currentCaseId, binding.toolbar.title.toString())
            Screen.ADDRESSES -> showCaseList()
            Screen.CASES -> super.onBackPressed()
        }
    }
}
