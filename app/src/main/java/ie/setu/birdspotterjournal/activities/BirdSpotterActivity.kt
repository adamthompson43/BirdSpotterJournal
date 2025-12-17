package ie.setu.birdspotterjournal.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.setu.birdspotterjournal.databinding.ActivityBirdspotterBinding
import ie.setu.birdspotterjournal.models.BirdModel
import timber.log.Timber
import timber.log.Timber.i
import android.app.DatePickerDialog
import java.util.Calendar
import ie.setu.birdspotterjournal.main.MainApp
import android.content.Intent
import java.util.UUID
import android.widget.ArrayAdapter
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.result.ActivityResultLauncher
import ie.setu.birdspotterjournal.models.Location

/**
 * BirdSpotterActivity allows users to add and delete birds
 * it provides input fields for bird species, location, notes, and date.
 * when the user saves, the activity returns the BirdModel object
 * back to BirdListActivity using an Intent result.
 */

class BirdSpotterActivity : AppCompatActivity() {
    // binding object for getting layout views
    private lateinit var binding: ActivityBirdspotterBinding
    // bird model instance for both new and edited birds
    var bird = BirdModel()

    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    lateinit var app: MainApp

    private fun registerMapCallback() {
        mapIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val loc = result.data!!.getParcelableExtra<Location>("location")!!
                    bird.geoLocation = loc
                    Snackbar.make(binding.root, "Map location saved", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private var photoUri: Uri? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                bird.imageUri = photoUri.toString()
                binding.birdImage.setImageURI(photoUri)
            }
        }

    private fun createImageUri(): Uri {
        val imagesDir = File(cacheDir, "images").apply { mkdirs() }
        val imageFile = File.createTempFile("bird_", ".jpg", imagesDir)

        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
    }

    // sets up layout, listeners, handles add and edit modes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirdspotterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerMapCallback()

        val birdSpeciesList = loadBirdSpeciesFromAssets().sorted()

        val speciesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            birdSpeciesList
        )

        binding.birdSpecies.setAdapter(speciesAdapter)

        binding.birdSpecies.threshold = 1

        app = application as MainApp

        // checking if an existing bird was passed in, if yes, populate ui fields and change button to "save"
        if (intent.hasExtra("bird_edit")) {
            bird = intent.getParcelableExtra("bird_edit")!!
            binding.birdSpecies.setText(bird.species)
            binding.birdLocation.setText(bird.placeName)
            binding.birdNotes.setText(bird.notes)
            binding.birdDate.setText(bird.date)

            if (bird.imageUri.isNotEmpty()) {
                binding.birdImage.setImageURI(Uri.parse(bird.imageUri))
            }

            binding.btnAdd.text = "Save"
            title = "Edit Bird"
        } else {
            title = "Add Bird"
        }

        // opening date picker when date field is clicked, date formatted and diplayed in edittext field
        binding.birdDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                binding.birdDate.setText(formattedDate)
                bird.date = formattedDate
            }, year, month, day)

            datePicker.show()
        }

        // handles add/save button click. validates species isnt empty, assigns uuid if new, returns birdmodel as an intent result
        binding.btnAdd.setOnClickListener {
            bird.species = binding.birdSpecies.text.toString()
            bird.placeName = binding.birdLocation.text.toString()
            bird.notes = binding.birdNotes.text.toString()
            bird.date = binding.birdDate.text.toString()

            if (bird.species.isNotEmpty()) {
                // give new birds a uuid
                if (bird.id.isEmpty()) {
                    bird.id = UUID.randomUUID().toString()
                }

                // preparing to be sent back to BirdListActivity
                val resultIntent = Intent()
                resultIntent.putExtra("bird_result", bird)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                // show snackbar if species is empty
                Snackbar.make(it, "Please enter a bird species", Snackbar.LENGTH_LONG).show()
            }
        }

        // handles cancel button
        binding.btnCancel.setOnClickListener {
            finish() // closes the current activity and returns to the previous one
        }

        binding.btnTakePhoto.setOnClickListener {
            photoUri = createImageUri()
            takePictureLauncher.launch(photoUri)
        }

        binding.btnPickLocation.setOnClickListener {
            val launcherIntent = Intent(this, MapActivity::class.java)
            launcherIntent.putExtra("location", bird.geoLocation)
            mapIntentLauncher.launch(launcherIntent)
        }
    }

    private fun loadBirdSpeciesFromAssets(): List<String> {
        return assets.open("birds.txt")
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
    }

}
