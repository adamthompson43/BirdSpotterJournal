package ie.setu.birdspotterjournal.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type
import java.util.UUID

/**
 * BirdJSONStore is a class that manages the storage and retrieval of bird data in JSON format.
 * Not going to lie, AI was used pretty heavily for this one
 *
 * It uses a Gson library to convert the bird data to and from JSON format.
 *
 */

private const val JSON_FILE = "birds.json"

class BirdJSONStore(private val context: Context) {

    // gson instance for converting JSON
    private val gson = Gson()

    // defining the type of the list
    private val listType: Type = object : TypeToken<MutableList<BirdModel>>() {}.type

    // in-memory bird list
    var birds = mutableListOf<BirdModel>()

    // checking if json file exists when store is created, if so, load that
    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    // returns all the stored birds
    fun findAll(): List<BirdModel> = birds

    // adding a new bird to the list, saving it to json file, giving it a uuid
    fun create(bird: BirdModel) {
        bird.id = UUID.randomUUID().toString()
        birds.add(bird)
        serialize()
    }

    // updates an existing bird in the list using it uuid, saving it to json file
    fun update(bird: BirdModel) {
        val index = birds.indexOfFirst { it.id == bird.id }
        if (index != -1) {
            birds[index] = bird
            serialize()
        }
    }

    // deletes bird by uuid
    fun delete(bird: BirdModel) {
        birds.removeIf { it.id == bird.id }
        serialize()
    }

    // serializes and deserializes the bird list to and from json file
    private fun serialize() {
        val jsonString = gson.toJson(birds, listType)
        File(context.filesDir, JSON_FILE).writeText(jsonString)
    }

    // reads the json file and converts it to a list of birds
    private fun deserialize() {
        val jsonString = File(context.filesDir, JSON_FILE).readText()
        birds = gson.fromJson(jsonString, listType)
    }

    // check if json file already exists
    private fun exists(context: Context, filename: String): Boolean {
        return File(context.filesDir, filename).exists()
    }
}
