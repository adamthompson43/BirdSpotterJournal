package ie.setu.birdspotterjournal.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.setu.birdspotterjournal.databinding.CardBirdBinding
import ie.setu.birdspotterjournal.models.BirdModel

class BirdAdapter constructor(
    private var birds: List<BirdModel>,
    private val onBirdClick: (BirdModel) -> Unit,
    private val onBirdDelete: (BirdModel) -> Unit
)
    : RecyclerView.Adapter<BirdAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBirdBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val bird = birds[holder.adapterPosition]
        holder.bind(bird, onBirdClick, onBirdDelete)
    }


    override fun getItemCount(): Int = birds.size

    class MainHolder(private val binding: CardBirdBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            bird: BirdModel,
            onBirdClick: (BirdModel) -> Unit,
            onBirdDelete: (BirdModel) -> Unit
        ) {
            binding.birdSpecies.text = bird.species
            binding.birdLocation.text = "Location: ${bird.placeName}"
            binding.birdDate.text = "Date: ${bird.date}"
            binding.birdNotes.text = "Notes: ${bird.notes}"

            // Edit when clicking the whole card
            binding.root.setOnClickListener { onBirdClick(bird) }

            // Delete when pressing the delete icon
            binding.deleteButton.setOnClickListener { onBirdDelete(bird) }
        }
    }
}