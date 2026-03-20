package com.example.pawcketdoc.ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pawcketdoc.R
import com.example.pawcketdoc.database.AppDatabase
import com.example.pawcketdoc.database.DatabaseProvider
import com.example.pawcketdoc.model.Document
import com.example.pawcketdoc.service.DocumentService
import com.example.pawcketdoc.service.UploadService
import com.example.pawcketdoc.util.SnackbarUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DocumentFormFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var documentService: DocumentService
    private lateinit var uploadService: UploadService

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var selectedFileUri: Uri? = null

    private val pickFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = requireContext().contentResolver.getType(it)
            if (mimeType != "application/pdf" && mimeType?.startsWith("image/") != true) {
                SnackbarUtil.showError(
                    view = requireView(),
                    title = "Invalid File",
                    message = "Only PDF and image files are allowed"
                )
                return@let
            }
            selectedFileUri = it
            val fileName = getFileName(it)
            view?.findViewById<TextInputEditText>(R.id.etFileName)?.setText(fileName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_document_form, container, false)

        val headerBar = requireActivity().findViewById<View>(R.id.headerBar)
        headerBar.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.surface)
        );
        return  view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, ime.bottom)
            insets
        }

        db = DatabaseProvider.getDatabase(requireContext())
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        documentService = DocumentService(db.documentDao(), firebaseFirestore, firebaseAuth)
        uploadService = UploadService(requireContext())

        val petId = arguments?.getString("pet_id")!!

        setupDocumentTypeDropdown(view)
        setupDatePicker(view)
        setupFilePicker(view)

        val etDocumentName = view.findViewById<TextInputEditText>(R.id.etDocumentName)
        val actvDocumentType = view.findViewById<AutoCompleteTextView>(R.id.actvDocumentType)
        val etDateIssued = view.findViewById<TextInputEditText>(R.id.etDateIssued)
        val etFileName = view.findViewById<TextInputEditText>(R.id.etFileName)
        val etNotes = view.findViewById<TextInputEditText>(R.id.etNotes)
        val buttonAddDocument = view.findViewById<Button>(R.id.buttonAddDocument)
        val progress = view.findViewById<ProgressBar>(R.id.progress)

        fun setLoading(isLoading: Boolean) {
            if (isLoading) {
                buttonAddDocument.text = ""
                buttonAddDocument.isEnabled = false
                progress.visibility = View.VISIBLE
            } else {
                buttonAddDocument.text = "Add Document"
                buttonAddDocument.isEnabled = true
                progress.visibility = View.GONE
            }
        }

        buttonAddDocument.setOnClickListener {
            var hasError = false

            if (etDocumentName.text.isNullOrBlank()) {
                etDocumentName.error = "Document name is required"
                hasError = true
            } else {
                etDocumentName.error = null
            }

            if (actvDocumentType.text.isNullOrBlank()) {
                actvDocumentType.error = "Document type is required"
                hasError = true
            } else {
                actvDocumentType.error = null
            }

            if (etDateIssued.text.isNullOrBlank()) {
                etDateIssued.error = "Date issued is required"
                hasError = true
            } else {
                etDateIssued.error = null
            }

            if (selectedFileUri == null) {
                etFileName.error = "Please attach a file"
                hasError = true
            } else {
                etFileName.error = null
            }

            if (hasError) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    setLoading(true)
                    val uploadResult = uploadService.uploadDocument(selectedFileUri!!)
                    val document = Document(
                        petId = petId,
                        name = etDocumentName.text.toString(),
                        type = actvDocumentType.text.toString(),
                        notes = etNotes.text.toString(),
                        fileUrl = uploadResult["secure_url"]!!,
                        publicId = uploadResult["public_id"]!!,
                        dateIssued = LocalDate.parse(etDateIssued.text.toString(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                    )
                    documentService.insert(document)
                    SnackbarUtil.showSuccess(
                        view = requireView(),
                        title = "Success",
                        message = "Document has been added"
                    )
                    findNavController().popBackStack()
                } catch (e: FirebaseNetworkException) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Network Error",
                        message = "No Internet Connection"
                    )
                } catch (e: Exception) {
                    SnackbarUtil.showError(
                        view = requireView(),
                        title = "Error",
                        message = e.message.toString()
                    )
                } finally {
                    setLoading(false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.txtHeaderTitle)
            .text = "New Document"
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
    }


    private fun setupDocumentTypeDropdown(root: View) {
        val documentTypes = listOf(
            "Vet Certificate",
            "Vaccination Record",
            "Adoption Papers",
            "Registration",
            "Medical Record",
            "Insurance",
            "Other"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            documentTypes
        )

        val dropdown = root.findViewById<AutoCompleteTextView>(R.id.actvDocumentType)
        dropdown.setAdapter(adapter)
    }

    private fun setupDatePicker(root: View) {
        val dateInput = root.findViewById<TextInputEditText>(R.id.etDateIssued)

        dateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date Issued")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "DATE_ISSUED_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = formatter.format(Date(selection))
                dateInput.setText(dateString)
            }
        }
    }

    private fun setupFilePicker(root: View) {
        val fileInput = root.findViewById<TextInputEditText>(R.id.etFileName)
        fileInput.setOnClickListener {
            pickFile.launch("*/*")
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Selected File"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        return name
    }
}