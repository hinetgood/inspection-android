package com.inspection.app.ui.addresses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inspection.app.InspectionApp
import com.inspection.app.R
import com.inspection.app.data.entity.Address
import com.inspection.app.databinding.FragmentAddressListBinding
import com.inspection.app.ui.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddressListFragment : Fragment() {
    private var _binding: FragmentAddressListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AddressAdapter
    private var caseId: Long = -1

    private val database by lazy { (requireActivity().application as InspectionApp).database }

    companion object {
        private const val ARG_CASE_ID = "case_id"

        fun newInstance(caseId: Long): AddressListFragment {
            return AddressListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CASE_ID, caseId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        caseId = arguments?.getLong(ARG_CASE_ID, -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddressListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AddressAdapter(
            onItemClick = { address ->
                (activity as? MainActivity)?.showPhotoList(address.id, address.address)
            },
            onDeleteClick = { address ->
                confirmDelete(address)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        loadAddresses()
    }

    private fun loadAddresses() {
        lifecycleScope.launch {
            database.addressDao().getAddressesByCase(caseId).collectLatest { addresses ->
                adapter.submitList(addresses)
                binding.emptyView.visibility = if (addresses.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (addresses.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    fun showAddAddressDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "完整地址"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("新增地址")
            .setView(editText)
            .setPositiveButton("確認") { _, _ ->
                val address = editText.text.toString().trim()
                if (address.isNotEmpty()) {
                    addAddress(address)
                } else {
                    Toast.makeText(context, "請輸入地址", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addAddress(addressStr: String) {
        lifecycleScope.launch {
            val address = Address(
                caseId = caseId,
                address = addressStr
            )
            database.addressDao().insert(address)
        }
    }

    private fun confirmDelete(address: Address) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("刪除地址")
            .setMessage("確定要刪除「${address.address}」嗎？所有相關照片都會被刪除。")
            .setPositiveButton("刪除") { _, _ ->
                lifecycleScope.launch {
                    database.addressDao().delete(address)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
