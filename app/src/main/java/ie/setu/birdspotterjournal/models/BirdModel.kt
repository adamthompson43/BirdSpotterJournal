package ie.setu.birdspotterjournal.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// using parcelize to pass data between activities using intents

@Parcelize
data class BirdModel(
    var id: String = "", // bird uuid string
    var species: String = "", // bird species name
    var location: String = "", // bird location name
    var notes: String = "", // additional notes on spotting
    var date: String = "", // date of spotting formatted to dd/mm/yyyy
    var imageUri: String = "" // image of bird
) : Parcelable
