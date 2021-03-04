package dev.snri.tracking.api

import S.N.R.I.tracking.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.animation.LinearInterpolator
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfMeasurement


/**
 * Add data to the map once the GeoJSON has been loaded
 *
 * @param featureCollection returned GeoJSON FeatureCollection from the Directions API route request
 */
class RideRoute (
        context: Context,
        mapboxMap: MapboxMap,
        origin: Point,
        destination: Point,
        routes: List<DirectionsRoute>
)  : Route(context, mapboxMap) {

    internal class PointEvaluator : TypeEvaluator<Point> {
        override fun evaluate(fraction: Float, startValue: Point, endValue: Point): Point {
            return Point.fromLngLat(
                startValue.longitude() + (endValue.longitude() - startValue.longitude()) * fraction,
                startValue.latitude() + (endValue.latitude() - startValue.latitude()) * fraction
            )
        }

    }

    private val DOT_SOURCE_ID = "dot-source-id"
    private val LINE_SOURCE_ID = "line-source-id"

    private lateinit var routeCoordinateList: List<Point>

    private lateinit var pointSource: GeoJsonSource
    private lateinit var lineSource: GeoJsonSource
    private val markerLinePointList = mutableListOf<Point>()
    private var routeIndex: Int = 0
    private var currentAnimator: Animator? = null

    init {
        val currentRoute: DirectionsRoute = routes[0]
        mapboxMap.getStyle { style ->
            mapboxMap.easeCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder()
                        .include(LatLng(origin.latitude(), origin.longitude()))
                        .include(LatLng(destination.latitude(), destination.longitude()))
                        .build(), 50
                ), 5000
            )

            var featureCollection = FeatureCollection.fromFeature(
                Feature.fromGeometry(
                    currentRoute.geometry()?.let {
                        LineString.fromPolyline(
                            it,
                            Constants.PRECISION_6
                        )
                    }
                )
            )

            if (featureCollection.features() != null) {
                val lineString = featureCollection.features()!![0].geometry() as LineString?
                if (lineString != null) {
                    routeCoordinateList = lineString.coordinates()
                    initSources(style, featureCollection)
                    initSymbolLayer(style)
                    initDotLinePath(style)
                }
            }
        }
    }

    /**
     * Add various sources to the map.
     */
    private fun initSources(loadedMapStyle: Style, featureCollection: FeatureCollection) {
        loadedMapStyle.removeLayer("symbol-layer-id")
        loadedMapStyle.removeSource(DOT_SOURCE_ID)
        loadedMapStyle.addSource(GeoJsonSource(DOT_SOURCE_ID, featureCollection).also { pointSource = it })

        loadedMapStyle.removeLayer("line-layer-id")
        loadedMapStyle.removeSource(LINE_SOURCE_ID)
        loadedMapStyle.addSource(GeoJsonSource(LINE_SOURCE_ID).also { lineSource = it })
    }

    /**
     * Add the marker icon SymbolLayer.
     */
    private fun initSymbolLayer(loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            "moving-red-marker", BitmapFactory.decodeResource(
                context.resources, R.drawable.pink_dot
            )
        )
        loadedMapStyle.addLayer(
            SymbolLayer("symbol-layer-id", DOT_SOURCE_ID).withProperties(
                iconImage("moving-red-marker"),
                iconSize(1f),
                iconOffset(arrayOf(5f, 0f)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
            )
        )
    }

    /**
     * Add the LineLayer for the marker icon's travel route. Adding it under the "road-label" layer, so that the
     * this LineLayer doesn't block the street name.
     */
    private fun initDotLinePath(loadedMapStyle: Style) {
        loadedMapStyle.addLayerBelow(
            LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#F13C6E")),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(4f)
            ), "road-label"
        )
    }

    /**
     * Set up the repeat logic for moving the icon along the route.
     */
    override fun animate() {
        // Check if we are at the end of the points list
        if ((routeCoordinateList.size - 1 > routeIndex)) {
            var indexPoint = routeCoordinateList.get(routeIndex)
            var newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude())
            currentAnimator = createLatLngAnimator(indexPoint, newPoint)
            currentAnimator?.start()
            routeIndex++
        }
    }

    private fun createLatLngAnimator(currentPosition: Point, targetPosition: Point): Animator? {
        val latLngAnimator = ValueAnimator.ofObject(
            RideRoute.PointEvaluator(),
            currentPosition,
            targetPosition
        )
        latLngAnimator.duration =
            TurfMeasurement.distance(currentPosition, targetPosition, "meters").toLong()
        latLngAnimator.interpolator = LinearInterpolator()
        latLngAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animate()
            }
        })
        latLngAnimator.addUpdateListener { animation ->
            val point = animation.animatedValue as Point
            pointSource.setGeoJson(point)
            markerLinePointList.add(point)
            lineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerLinePointList)))
        }
        return latLngAnimator
    }

    fun clear(style: Style) {
        style.removeSource(DOT_SOURCE_ID)

        style.removeSource(LINE_SOURCE_ID)

    }
}