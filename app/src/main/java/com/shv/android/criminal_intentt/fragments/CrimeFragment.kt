package com.shv.android.criminal_intentt.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.shv.android.criminal_intentt.Crime
import com.shv.android.criminal_intentt.CrimeDetailViewModel
import com.shv.android.criminal_intentt.R
import com.shv.android.criminal_intentt.getScaledBitmap
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"

private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_IMAGE = "DialogImage"

const val RESULT_DATE_KEY = "data_result"
const val RESULT_TIME_KEY = "time_result"
const val RESULT_IMAGE = "image_result"

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment(),
    DatePickerFragment.Callbacks,
    TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private var photoFile: File? = null
    private lateinit var photoUri: Uri

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var phoneButton: Button
    private lateinit var photoView: ImageView
    private lateinit var cameraButton: ImageButton

    private lateinit var viewTreeObserver: ViewTreeObserver
    private var viewWidth = 0
    private var viewHeight = 0

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        val crimeId: UUID? = if (Build.VERSION.SDK_INT >= 33)
            arguments?.getSerializable(ARG_CRIME_ID, UUID::class.java)
        else
            arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId!!)

        setFragmentResultListener(RESULT_DATE_KEY) { _, bundle ->
            val date = if (Build.VERSION.SDK_INT >= 33)
                bundle.getSerializable("date", Date::class.java) as Date
            else
                bundle.getSerializable("date") as Date
            onDateSelected(date)
            Log.i(TAG, "Date selected: $date")
        }

        setFragmentResultListener(RESULT_TIME_KEY) { _, bundle ->
            val hour = bundle.getInt("hour")
            val minute = bundle.getInt("minute")
            onTimeSelected(hour, minute)
            Log.i(TAG, "Time selected: $hour:$minute")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date_button) as Button
        timeButton = view.findViewById(R.id.crime_time_button) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        phoneButton = view.findViewById(R.id.crime_phone) as Button
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        cameraButton = view.findViewById(R.id.crime_camera) as ImageButton
        solvedCheckBox = view.findViewById(R.id.crime_solved_check_box) as CheckBox

        viewTreeObserver = photoView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener {
            viewWidth = photoView.width
            viewHeight = photoView.height
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.shv.android.criminal_intentt.fileprovider",
                    photoFile!!
                )
                updateUI()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //nothing
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //nothing
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        photoView.apply {
            setOnClickListener {
                if (photoFile?.exists() == true) {
                    photoFile?.let { it1 ->
                        ImagePickerFragment.newInstance(it1).apply {
                            show(this@CrimeFragment.parentFragmentManager, DIALOG_IMAGE)
                        }
                    }
                }
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                getContactName.launch(pickContactIntent)
            }
        }

        phoneButton.apply {
            val pickPhoneNumber =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            setOnClickListener {
                getContactNumber.launch(pickPhoneNumber)
            }
        }

        cameraButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //РАЗОБРАТЬСЯ С Deprecated классами
//            val resolvedActivity: ResolveInfo? =
//                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
//            if (resolvedActivity == null) {
//                isEnabled = false
//            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                getCamera.launch(captureImage)
            }

        }

        photoView.viewTreeObserver.apply {
            if (isAlive) {
                addOnGlobalLayoutListener {
                    updatePhotoView()
                }
            }
        }
    }

    private val getCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if ((result.resultCode == Activity.RESULT_OK) && (result.data != null)) {
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                if (photoFile?.exists() == true) {
                    updatePhotoView()
                }
            }
        }

    private val getContactName =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if ((result.resultCode == Activity.RESULT_OK) && (result.data != null)) {
                val contactUri: Uri? = result.data?.data
                val queryField = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(contactUri, queryField, null, null, null)
                }
                cursor?.use {
                    if (it.count == 0)
                        return@use
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }

    @SuppressLint("Range")
    private val getContactNumber =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if ((result.resultCode == Activity.RESULT_OK) && (result.data != null)) {
                val contactUri: Uri? = result.data?.data
                val queryFields = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)
                }
                cursor?.use {
                    if (it.count == 0)
                        return@use
                    it.moveToFirst()
                    val isHasNumber =
                        it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER))
                            .toInt() > 0
                    val number = if (isHasNumber) it.getString(1) else -1
                    val numToCall = Uri.parse("tel:$number")
                    val intent = Intent(Intent.ACTION_DIAL, numToCall)
                    startActivity(intent)
                }
            }
        }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
            getString(R.string.crime_report_solved)
        else
            getString(R.string.crime_report_unsolved)

        val dataString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank())
            getString(R.string.crime_report_no_suspect)
        else
            getString(R.string.crime_report_suspect, crime.suspect)

        return getString(
            R.string.crime_report, crime.title,
            dataString, solvedString, suspect
        )
    }

    private fun updatePhotoView() {
        if (photoFile?.exists() == true) {
            val bitmap = getScaledBitmap(photoFile!!.path, photoView.width, photoView.height)
            photoView.rotation = 90F
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageBitmap(null)
        }
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = DateFormat.format("EEEE, LLL dd, yyyy", crime.date).toString()
        timeButton.text = DateFormat.format("HH:mm", crime.date).toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty())
            suspectButton.text = crime.suspect
        updatePhotoView()
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(h: Int, m: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = crime.date

        calendar.set(Calendar.HOUR_OF_DAY, h)
        calendar.set(Calendar.MINUTE, m)
        crime.date = calendar.time
        updateUI()
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}