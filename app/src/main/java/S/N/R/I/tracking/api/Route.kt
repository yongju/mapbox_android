package S.N.R.I.tracking.api

import android.content.Context
import com.mapbox.mapboxsdk.maps.MapboxMap

abstract class Route (
        protected var context: Context,
        protected var mapboxMap: MapboxMap
){

    public abstract fun animate();
}