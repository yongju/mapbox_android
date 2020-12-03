package S.N.R.I.tracking.api

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.maps.MapboxMap

class TrackingRoute(
        context: Context,
        mapboxMap: MapboxMap,
        routeCoordinates : MutableList<Point>
) : Route(context, mapboxMap) {

    init {
        
    }

    override fun animate() {
        TODO("Not yet implemented")
    }

}