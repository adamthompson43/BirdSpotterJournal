package ie.setu.birdspotterjournal.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.birdspotterjournal.databinding.ActivityBirdListBinding
import ie.setu.birdspotterjournal.databinding.CardBirdBinding
import ie.setu.birdspotterjournal.main.MainApp
import ie.setu.birdspotterjournal.models.BirdModel
import timber.log.Timber.i
import android.view.Menu
import ie.setu.birdspotterjournal.R
import android.content.Intent
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import ie.setu.birdspotterjournal.adapters.BirdAdapter

/**
 * BirdListActivity displays a list of stored bird in the app
 * Allows users to:
 *  - View all birds in a recycle view
 *  - Add a new bird
 *  - Edit or delete existing birds
 */

class BirdListActivity : AppCompatActivity() {

    lateinit var app: MainApp

    // binding object for getting layout views
    private lateinit var binding: ActivityBirdListBinding

    // initialising the activity, setting up toolbar, recyclerview and clikc listeners
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirdListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // getting reference to application level calss
        app = application as MainApp
        i("BirdList Activity started... total birds: ${app.birdsStore.birds.size}")

        // configure toolbar
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        // setting up the recyclerview to show bird cards
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager


        // creating adapter with click and delete handlres
        binding.recyclerView.adapter = BirdAdapter(
            app.birdsStore.birds,
            onBirdClick = { bird ->
                // opening birdspotteractivity in edit mode
                val launcherIntent = Intent(this, BirdSpotterActivity::class.java)
                launcherIntent.putExtra("bird_edit", bird)
                getResult.launch(launcherIntent)
            },
            onBirdDelete = { bird ->
                // deletes bird from JSON store
                app.birdsStore.delete(bird)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        )
    }

    // inflating toolbar to add + icon
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handling toolbar menu selections, when + is clicked, opens birdspotteractivity in add mode
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, BirdSpotterActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // handling activity result from birdspotteractivity
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                // retrieving birdmodel passed back from birdspotteractivity
                val bird = result.data!!.getParcelableExtra<BirdModel>("bird_result") ?: return@registerForActivityResult

                // checking if the bird already exists (edit mode)
                val existingIndex = app.birdsStore.birds.indexOfFirst { it.id == bird.id }

                if (existingIndex != -1) {
                    // bird exists = update existing bird
                    app.birdsStore.update(bird)
                    binding.recyclerView.adapter?.notifyItemChanged(existingIndex)
                } else {
                    // bird is new = create it
                    app.birdsStore.create(bird)
                    binding.recyclerView.adapter?.notifyItemInserted(app.birdsStore.birds.size - 1)
                }
            }
        }

}

