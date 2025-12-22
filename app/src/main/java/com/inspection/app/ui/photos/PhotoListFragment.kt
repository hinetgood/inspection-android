package com.inspection.app.ui.photos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inspection.app.InspectionApp
import com.inspection.app.R
import com.inspection.app.data.entity.Photo
import com.inspection.app.databinding.FragmentPhotoListBinding
import com.inspection.app.ui.camera.CameraActivity
import com.inspection.app.util.PdfExporter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class PhotoListFragment : Fragment() {
    private var _binding: FragmentPhotoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PhotoAdapter
    private var addressId: Long = -1
    private var addressName: String = ""

    private val database by lazy { (requireActivity().application as InspectionApp).database }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoPath = result.data?.getStringExtra(CameraActivity.EXTRA_PHOTO_PATH)
            if (photoPath != null) {
                showEditPhotoDialog(photoPath)
            }
        }
    }

    companion object {
        private const val ARG_ADDRESS_ID = "address_id"
        private const val ARG_ADDRESS_NAME = "address_name"

        fun newInstance(addressId: Long, addressName: String): PhotoListFragment {
            return PhotoListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ADDRESS_ID, addressId)
                    putString(ARG_ADDRESS_NAME, addressName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addressId = arguments?.getLong(ARG_ADDRESS_ID, -1) ?: -1
        addressName = arguments?.getString(ARG_ADDRESS_NAME, "") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPhotoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PhotoAdapter(
            onItemClick = { photo -> showEditPhotoDialog(photo) },
            onDeleteClick = { photo -> confirmDeletePhoto(photo) }
        )

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        binding.btnExport.setOnClickListener { exportPdf() }

        loadPhotos()
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            database.photoDao().getPhotosByAddress(addressId).collectLatest { photos ->
                adapter.submitList(photos)
                binding.emptyView.visibility = if (photos.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (photos.isEmpty()) View.GONE else View.VISIBLE
                binding.tvPhotoCount.text = "${photos.size} 張照片"
            }
        }
    }

    fun takePhoto() {
        val intent = Intent(requireContext(), CameraActivity::class.java).apply {
            putExtra(CameraActivity.EXTRA_ADDRESS_ID, addressId)
            putExtra(CameraActivity.EXTRA_ADDRESS_NAME, addressName)
        }
        cameraLauncher.launch(intent)
    }

    private fun showEditPhotoDialog(photoPath: String) {
        lifecycleScope.launch {
            val photos = database.photoDao().getPhotosByAddressOnce(addressId)
            val sequence = photos.size + 1

            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_photo, null)
            // TODO: Setup dialog views and save photo

            val photo = Photo(
                addressId = addressId,
                sequence = sequence,
                originalPath = photoPath,
                watermarkedPath = photoPath, // Will be processed
                position = "牆",
                material = "P"
            )

            database.photoDao().insert(photo)
            Toast.makeText(context, "照片已儲存", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditPhotoDialog(photo: Photo) {
        // TODO: Show edit dialog for existing photo
        Toast.makeText(context, "編輯照片 #${photo.sequence}", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDeletePhoto(photo: Photo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("刪除照片")
            .setMessage("確定要刪除照片 #${photo.sequence} 嗎？")
            .setPositiveButton("刪除") { _, _ ->
                lifecycleScope.launch {
                    database.photoDao().delete(photo)
                    // Delete file
                    File(photo.originalPath).delete()
                    File(photo.watermarkedPath).delete()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun exportPdf() {
        lifecycleScope.launch {
            try {
                binding.btnExport.isEnabled = false
                binding.btnExport.text = "匯出中..."

                val address = database.addressDao().getAddressById(addressId)
                val photos = database.photoDao().getPhotosByAddressOnce(addressId)

                if (photos.isEmpty()) {
                    Toast.makeText(context, "沒有照片可匯出", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val exporter = PdfExporter(requireContext())
                val pdfFile = exporter.exportPhotos(address, photos)

                Toast.makeText(context, "PDF 已儲存: ${pdfFile.name}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "匯出失敗: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnExport.isEnabled = true
                binding.btnExport.text = "匯出 PDF"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
