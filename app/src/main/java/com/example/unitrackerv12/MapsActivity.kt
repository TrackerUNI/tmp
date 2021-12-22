package com.example.unitrackerv12

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.unitrackerv12.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var databaseRef: DatabaseReference

    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var GroupIDTest : String? = null

    //PEDIR PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mMap.isMyLocationEnabled = true
                getLocationAccess()
            } else {
                Toast.makeText(
                    this,
                    "User has not granted location access permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    //COMPROBAR PERMISOS
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //
        databaseRef = Firebase.database.reference
        databaseRef.addValueEventListener(logListener)
        btnmngTracking.setOnClickListener {

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //
        mMap.uiSettings.isZoomControlsEnabled = true

        getLocationAccess()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    fun createMarker(lat : Double, lng : Double, username : String){
        val ubi = LatLng(lat, lng)
        mMap.addMarker(MarkerOptions()
            .position(ubi)
            .title(username))
            ?.setIcon(BitmapDescriptorFactory.defaultMarker(Random.nextInt(360).toFloat()))
    }


    private fun GetLocationGroup(groupid: String) {
        var groupData: GroupData? = null /*GroupManager.get(groupid)*/
        var lastPositions: MutableMap<String?, Position?> = mutableMapOf()

        var doc = GroupManager.collection.document(groupid)
        doc.get()
            .addOnSuccessListener { documentSnapshot ->
                groupData = documentSnapshot.toObject(GroupData::class.java)
                Log.d(TAG, "Positions group: ${groupid}")
                mMap.clear()
                groupData!!.users?.forEach { userid ->
                    UserManagerV.collection.document(userid)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            var userData = documentSnapshot.toObject(UserData::class.java)
                            var position: Position? = userData?.lastPosition
                            lastPositions[userData!!.username] = position
                            Log.d(TAG, "Position user ${userData.userid} (${userData.username}): (${position!!.latitude}, ${position!!.longitude})")
                            userData!!.username?.let {
                                createMarker(position.latitude, position.longitude,
                                    it
                                )
                            }
                        }
                }
            }
    }


    //FIREBASE: Enviar datos a la bd
    private fun getLocationUpdates() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        //GroupManager.create("Familia")
        btnGroup.setOnClickListener{
            if (id_grrupo.text.isNullOrEmpty())
            //Log.e(Tag,"Debes ingresar un GroupID")
            else {
                GroupIDTest=id_grrupo.text.toString()
            }
        }
        //veriifcar si el id del grupo no e nulo y asignalor a GroupIdTest









        //-------------------------------FIREBASE---------------------------------------------
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    val p = Position(location.latitude,location.longitude)
                    UserManagerV.addPosition(auth.currentUser, p )
                    GroupIDTest?.let { GetLocationGroup(it) }

                }
            }
        }
    }

    //FIREBASE: Pedir los datos y error en leer bdFirebase
    val logListener = object : ValueEventListener {
        //FIREBASE: Pedir datos a la bd
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                //MODIFICAR referencia en bdFirebase
                val ulocation = dataSnapshot.child("userlocation").getValue(bdLocation::class.java)
                var Lat=ulocation?.Latitude
                var Long=ulocation?.Longitude
                var uName=ulocation?.uName

                if (Lat !=null  && Long != null) {
                    val Loc = LatLng(Lat, Long)
                    val markerOptions = MarkerOptions().position(Loc).title(uName)
                    mMap.addMarker(markerOptions)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Loc, 10f))
                    Toast.makeText(applicationContext, "Usuario encontrado", Toast.LENGTH_LONG).show()
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Error al leer datos", Toast.LENGTH_LONG)
                .show()
        }


    }

}