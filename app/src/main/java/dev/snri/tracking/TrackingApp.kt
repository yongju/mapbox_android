package dev.snri.tracking

import S.N.R.I.tracking.BuildConfig
import S.N.R.I.tracking.R
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