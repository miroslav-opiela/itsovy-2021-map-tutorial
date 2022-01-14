package sk.itsovy.android.maptutorial

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.util.CollectionUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import sk.itsovy.android.maptutorial.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationProviderClient : FusedLocationProviderClient
    private var currentPosition = LatLng(48.7, 21.3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fabLocation.setOnClickListener {
            getLocation()
        }

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // mam FINE permission
                    Log.d("MAPTUTORIAL", "FINE permission")
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // mam COARSE permission
                    Log.d("MAPTUTORIAL", "COARSE permission")
                }
                else -> {
                    // nemam permission
                    Log.d("MAPTUTORIAL", "NO permission")
                }
            }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        // val sydney = LatLng(48.0, 28.0)

        map.addMarker(MarkerOptions().position(currentPosition).title("Marker"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 10f))
        //map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        //map.isTrafficEnabled = true

        map.setOnMapClickListener {
            currentPosition = it
            val address = getAddress()
            Log.d("ADRESA", address)
            map.addMarker(MarkerOptions().position(currentPosition).title(address))

        }

    }

    private fun getAddress() : String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val fromLocation = geocoder.getFromLocation(currentPosition.latitude, currentPosition.longitude, 1)
        if (CollectionUtils.isEmpty(fromLocation)) {
            return "Address Unknown"
        }
        val address = fromLocation[0]
        return address.getAddressLine(0)
    }


    private fun getLocation() {
         if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // rationale - poziadat detailnejsie o povolenie
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        val task = locationProviderClient.lastLocation
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "LOCATION OK", Toast.LENGTH_SHORT).show()

                map.clear()
                val location = it.result
                currentPosition = LatLng(location.latitude, location.longitude)
                map.addMarker(MarkerOptions().position(currentPosition).title("Location"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14f))


            } else {
                Toast.makeText(this, "NO LOCATION", Toast.LENGTH_SHORT).show()
            }
        }
    }
}