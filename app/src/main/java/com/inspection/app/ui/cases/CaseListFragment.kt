package com.inspection.app.ui.cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inspection.app.InspectionApp
import com.inspection.app.R
import com.inspection.app.data.entity.Case
import com.inspection.app.ui.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CaseListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var adapter: CaseAdapter

    private val database by lazy { (requireActivity().application as InspectionApp).database }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_case_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)

        adapter = CaseAdapter(
            onItemClick = { case ->
                (activity as? MainActivity)?.showAddressList(case.id, case.caseNumber)
            },
            onDeleteClick = { case ->
                confirmDelete(case)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        loadCases()
    }

    private fun loadCases() {
        lifecycleScope.launch {
            database.caseDao().getAllCases().collectLatest { cases ->
                adapter.submitList(cases)
                emptyView.visibility = if (cases.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (cases.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    fun showAddCaseDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "案件名稱"
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("新增案件")
            .setView(editText)
            .setPositiveButton("確認") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    addCase(name)
                } else {
                    Toast.makeText(context, "請輸入案件名稱", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addCase(name: String) {
        lifecycleScope.launch {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val case = Case(
                caseNumber = name,
                caseName = name,
                caseDate = dateFormat.format(Date())
            )
            database.caseDao().insert(case)
        }
    }

    private fun confirmDelete(case: Case) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("刪除案件")
            .setMessage("確定要刪除「${case.caseNumber}」嗎？所有相關資料都會被刪除。")
            .setPositiveButton("刪除") { _, _ ->
                lifecycleScope.launch {
                    database.caseDao().delete(case)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
