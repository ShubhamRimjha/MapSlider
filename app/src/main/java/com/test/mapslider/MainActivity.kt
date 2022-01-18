package com.test.mapslider

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.test.mapslider.Room.pointsDB.PointDB
import com.test.mapslider.Room.pointsDB.PointEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private val REQUEST_CODE_PERMISSIONS = 101;
    lateinit var googleMap: GoogleMap
    var selfLatLng: LatLng = LatLng(21.204794, 79.067607)
    lateinit var mapFragment: SupportMapFragment
    lateinit var locationManager: LocationManager
    lateinit var db: RoomDatabase
    var progressView: ViewGroup? = null
    var isProgressShowing = false
    var radius: Int = Integer.MAX_VALUE
    lateinit var radioGroup: RadioGroup
    var pointArray: List<PointEntity>? = null

    lateinit var frameLayout: FrameLayout

    // initializing
    // FusedLocationProviderClient
    // object
    var mFusedLocationClient: FusedLocationProviderClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_map)
        //ask for location permission
        radioGroup = findViewById(R.id.rg_radius)
        frameLayout = findViewById(R.id.root_view)
        initializeMap()
        //        if (!permissionsGranted()) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//                REQUEST_CODE_PERMISSIONS
//            )
//        } else {
//            getSelfLoc()
//        }

    }

    private fun initializeMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

//    private fun permissionsGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PERMISSION_GRANTED
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 123) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
//                // Permission granted.
//                getSelfLoc()
//            } else {
//                // User refused to grant permission. You can add AlertDialog here
//                Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show()
//                this.finish()
//            }
//        }
//    }

//    @SuppressLint("MissingPermission")
//    fun getSelfLoc() {
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        val locaTask = mFusedLocationClient?.lastLocation
//        locaTask?.addOnSuccessListener {
//            Log.i("Self Location Success", "getSelfLoc: $it")
//            selfLatLng = LatLng(it.latitude, it.longitude)
//        }
//        initializeMap()
//    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.setOnMapLongClickListener(this)
        Log.i("TAG MAP", "onMapReady: Map Loaded")
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selfLatLng, 10f))
        loadPoints()
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_1 -> radius = 1
                R.id.rb_2 -> radius = 2
                R.id.rb_5 -> radius = 5
                R.id.rb_10 -> radius = 10
                R.id.rb_all -> radius = Int.MAX_VALUE
            }
            pointArray?.let { plotPoints(it) }
            drawCircle(radius*1000)
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        // Creating a marker
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            db = Room.databaseBuilder(this@MainActivity, PointDB::class.java, "database_of_points")
                                .build()
                            (db as PointDB).pointDAO().insertPoint(
                                PointEntity(
                                    System.currentTimeMillis().toInt(),
                                    latLng.latitude,
                                    latLng.longitude
                                )
                            )
                            db.close()
                        }
                        val markerOptions = MarkerOptions().position(latLng)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                        googleMap.addMarker(markerOptions)
                        loadPoints()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()

    }

    fun showProgressingView() {
        if (!isProgressShowing) {
            isProgressShowing = true
            progressView = layoutInflater.inflate(R.layout.progress_bar_layout, null) as ViewGroup
            val viewGroup = findViewById<View>(R.id.root_view) as ViewGroup
            viewGroup.addView(progressView)
        }
    }

    fun hideProgressingView() {
        val viewGroup = findViewById<View>(R.id.root_view) as ViewGroup
        viewGroup.removeView(progressView)
        isProgressShowing = false
    }

    fun loadPoints() {
        showProgressingView()
        CoroutineScope(Dispatchers.IO).launch {
            db = Room.databaseBuilder(this@MainActivity, PointDB::class.java, "database_of_points")
                .build()
            pointArray = (db as PointDB).pointDAO().getAllPoints()
            if (!pointArray.isNullOrEmpty()) plotPoints(pointArray!!)
            else
                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, "No points to show, long click to add", Toast.LENGTH_SHORT).show()
                })
        }
        hideProgressingView()
    }

    private fun drawCircle(radius: Int) {
        // Instantiating CircleOptions to draw a circle around the marker
        googleMap.addCircle(
            CircleOptions()
                .center(LatLng(selfLatLng.latitude, selfLatLng.longitude))
                .radius(radius.toDouble())
                .strokeColor(Color.RED)
                .fillColor(Color.parseColor("#68F8F804"))
                .strokeWidth(1.0f)
        )
    }

    fun plotPoints(points: List<PointEntity>) {
        this@MainActivity.runOnUiThread(Runnable {
            googleMap.clear()
            for (p in points) {
                val result = FloatArray(5)
                android.location.Location.distanceBetween(
                    selfLatLng.latitude,
                    selfLatLng.longitude,
                    p.lat,
                    p.lng,
                    result
                )
                if (result[0]/1000 < radius ) {
                    googleMap.addMarker(MarkerOptions().position(LatLng(p.lat, p.lng)).title(p.point_id.toString()))
                }
            }
        })
    }
}