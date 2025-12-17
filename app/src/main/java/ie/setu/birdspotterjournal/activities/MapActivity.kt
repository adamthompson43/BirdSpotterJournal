package ie.setu.birdspotterjournal.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ie.setu.birdspotterjournal.R
import ie.setu.birdspotterjournal.databinding.ActivityMapBinding
import ie.setu.birdspotterjournal.models.Location
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * MapActivity is a map that allows the user to click on any point and add
 * a location of a bird sighting
 */

class MapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener,
    GoogleMap.OnMapClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 1001

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding

    private var location = Location()
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("location")) {
            location = intent.getParcelableExtra("location")!!
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSaveLocation.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("location", location)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapClickListener(this)

        enableMyLocation()

        val pos = LatLng(location.lat, location.lng)

        marker = mMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title("Bird Spot")
                .draggable(true)
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, location.zoom))
    }

    private fun enableMyLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            // Only move camera if this is a NEW bird (default location)
            if (location.lat == 52.245696 && location.lng == -7.139102) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val userLatLng = LatLng(loc.latitude, loc.longitude)

                        marker?.position = userLatLng
                        location.lat = loc.latitude
                        location.lng = loc.longitude

                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                        )
                    }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        }
    }



    override fun onMapClick(latLng: LatLng) {
        marker?.position = latLng
        location.lat = latLng.latitude
        location.lng = latLng.longitude
    }

    override fun onMarkerDragEnd(m: Marker) {
        location.lat = m.position.latitude
        location.lng = m.position.longitude
        location.zoom = mMap.cameraPosition.zoom
    }

    override fun onMarkerDrag(m: Marker) {}
    override fun onMarkerDragStart(m: Marker) {}
}
