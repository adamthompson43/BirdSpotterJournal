package ie.setu.birdspotterjournal.main

import android.app.Application
import ie.setu.birdspotterjournal.models.BirdJSONStore
import timber.log.Timber
import timber.log.Timber.i

/**
 * Main application class for the app.
 * Initializes the Timber logging library and creates an instance of the BirdJSONStore.
 * This store is used to store and manage bird data.
 */
class MainApp : Application() {

    // global instance of birdjsonstore
    lateinit var birdsStore: BirdJSONStore

    // called when app starts, initialises timber and creates birdstore
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("BirdSpotter started")

        birdsStore = BirdJSONStore(this)
    }
}


