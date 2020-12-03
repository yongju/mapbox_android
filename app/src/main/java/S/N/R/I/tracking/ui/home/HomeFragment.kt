package S.N.R.I.tracking.ui.home

import S.N.R.I.tracking.R
import S.N.R.I.tracking.TrackingApp
import S.N.R.I.tracking.databinding.FragmentHomeBinding
import S.N.R.I.tracking.api.RideRoute
import S.N.R.I.tracking.api.Route
import S.N.R.I.tracking.api.TrackingRoute
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.MapboxConstants
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class HomeFragment : Fragment(), OnMapReadyCallback {

    private val TAG: String = HomeFragment::class.simpleName.toString()

    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

//    private val originPoint: Point = Point.fromLngLat(38.7508, 9.0309)
//    private val destinationPoint: Point = Point.fromLngLat(38.795902, 8.984467)
    private val originPoint: Point = Point.fromLngLat(126.8599272879938, 37.51964107214765)
    private val destinationPoint: Point = Point.fromLngLat(126.829706766276, 37.563498024659594)

    private lateinit var mapboxMap: MapboxMap
    private var rideRoute: Route? = null
    private var routeCoordinates = mutableListOf<Point>();

    /**
     * Location callback
     */
    private val locationEngineCallback: LocationEngineCallback<LocationEngineResult> = object: LocationEngineCallback<LocationEngineResult> {

        override fun onSuccess(result: LocationEngineResult?) {
            Timber.d("LocationEngineCallback.onSuccess() %s", result?.lastLocation)
            routeCoordinates.add(Point.fromLngLat(result!!.lastLocation!!.latitude, result!!.lastLocation!!.longitude))
        }

        override fun onFailure(exception: Exception) {
            Timber.e(exception, "LocationEngineCallback.onFailure() ")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true);
//        homeViewModel =
//                ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().invalidateOptionsMenu()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_locations -> onActionLocation();
            R.id.action_route -> onActionRoute(originPoint, destinationPoint)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    private fun onActionLocation() {
/*
        // Get an instance of the component
        val locationComponent: LocationComponent = mapboxMap.locationComponent

        locationComponent.isLocationComponentEnabled = !locationComponent.isLocationComponentEnabled

        if (locationComponent.isLocationComponentEnabled) {
            initLocationEngine()
        } else {
            locationComponent.locationEngine?.removeLocationUpdates(locationEngineCallback)
        }
*/
        Toast.makeText(requireContext(), "locations = " + routeCoordinates, Toast.LENGTH_LONG).show()
        // Get the directions rideRoute
        rideRoute = TrackingRoute(requireContext(), mapboxMap, routeCoordinates)
        rideRoute?.animate()

    }

    private fun onActionRoute(origin: Point, destination: Point) {

        val client: MapboxDirections = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                System.out.println(call.request().url().toString())

                // You can get the generic HTTP info about the response
                Timber.d("Response code: %s", response.code())
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Timber.e("No routes found")
                    return
                }
                var routes = response.body()!!.routes()
                Timber.d("routes $routes")

                // Get the directions rideRoute
                rideRoute = RideRoute(requireContext(), mapboxMap, origin, destination, routes)
                rideRoute?.animate()
            }

            override fun onFailure(call: Call<DirectionsResponse?>?, throwable: Throwable) {
                Timber.e("Error: %s", throwable.message)
                Toast.makeText(
                    requireContext(), "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.OUTDOORS) {

            /* init localization */
            val localizationPlugin = LocalizationPlugin(binding.map, mapboxMap, it)

            try {
                localizationPlugin.matchMapLanguageWithDeviceDefault()
            } catch (exception: RuntimeException) {
                Timber.d(exception.toString())
            }

            if ((requireActivity().application as TrackingApp).enableLocationComponent) {
                initLocationEngine()
                enableLocationComponent(it)
            }

            /* move current location */
            var location: Location? = mapboxMap.locationComponent.lastKnownLocation
            val position = CameraPosition.Builder()
                .target(LatLng(location!!.latitude, location!!.longitude)) // Sets the new camera position
                .zoom(16.0) // Sets the zoom
                .bearing(location.bearing.toDouble()) // Rotate the camera
                .tilt(MapboxConstants.MAXIMUM_TILT) // Set the camera tilt
                .build() // Creates a CameraPosition from the builder


            mapboxMap.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(position), 1000
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        val locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper())
        locationEngine.getLastLocation(locationEngineCallback)
    }
    
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {

        // Get an instance of the component
        val locationComponent: LocationComponent = mapboxMap.locationComponent

        // Activate with options
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(this.requireContext(), loadedMapStyle)
                .build()
        )

        // Enable to make component visible
        locationComponent.isLocationComponentEnabled = true

        // Set the component's camera mode
        locationComponent.cameraMode = CameraMode.TRACKING_COMPASS

        // Set the component's render mode
        locationComponent.renderMode = RenderMode.COMPASS

    }

}