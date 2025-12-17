package ie.setu.birdspotterjournal.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ie.setu.birdspotterjournal.R
import ie.setu.birdspotterjournal.databinding.ActivityBirdMapBinding
import ie.setu.birdspotterjournal.main.MainApp
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * BirdMapActivity displays a map of stored birds in the app
 * Allows users to:
 *  - View all birds in a map
 */

class BirdMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 1001

    // binding object for getting layout views
    private lateinit var binding: ActivityBirdMapBinding
    // google map object
    private lateinit var mMap: GoogleMap
    // application level class
    private lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        app = application as MainApp

        binding = ActivityBirdMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()

        val birds = app.birdsStore.findAll()

        if (birds.isNotEmpty()) {
            birds.forEach { bird ->
                val loc = bird.geoLocation
                val position = LatLng(loc.lat, loc.lng)

                mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(bird.species)
                        .snippet(bird.placeName)
                )
            }

            // Zoom to first bird
            val first = birds.first().geoLocation
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(first.lat, first.lng),
                    first.zoom
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_list -> {
                startActivity(Intent(this, BirdListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            renderMarkers()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    private fun renderMarkers() {
        mMap.clear()

        val birds = app.birdsStore.findAll()

        for (bird in birds) {
            val loc = bird.geoLocation
            val position = LatLng(loc.lat, loc.lng)

            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(bird.species)
                    .snippet(bird.placeName)
            )
        }
    }

    private fun enableMyLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            14f
                        )
                    )
                }
            }

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

}
