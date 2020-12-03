package S.N.R.I.tracking

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox
import timber.log.Timber

class TrackingApp: Application() {

    var enableLocationComponent: Boolean = false

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(Timber.DebugTree())
        }

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
    }
}