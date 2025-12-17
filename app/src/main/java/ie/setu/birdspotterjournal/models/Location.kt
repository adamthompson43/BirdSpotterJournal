package ie.setu.birdspotterjournal.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    var lat: Double = 52.245696,
    var lng: Double = -7.139102,
    var zoom: Float = 15f
) : Parcelable
