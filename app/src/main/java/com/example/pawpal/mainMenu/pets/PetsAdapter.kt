package com.example.pawpal.mainMenu.pets

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class PetAdapter(
    private val pets: List<Pet>,
    private val onItemClick: (Pet) -> Unit,
    private val onItemLongClick: ((Pet) -> Unit)? = null
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petAvatar: CircleImageView = itemView.findViewById(R.id.pet_avatar)
        val petName: TextView = itemView.findViewById(R.id.pet_name_item)
        val petType: TextView = itemView.findViewById(R.id.pet_type_item)
        val petAge: TextView = itemView.findViewById(R.id.pet_age_item)
        val editButton: Button = itemView.findViewById(R.id.edit_pet_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]

        holder.petName.text = pet.name
        holder.petType.text = "${pet.sex} ${pet.type} • ${pet.breed}"
        holder.petAge.text = "${pet.age} Year${if (pet.age != 1) "s" else ""} Old"

        // Load pet photo
        if (pet.photoPath != null && File(pet.photoPath).exists()) {
            holder.petAvatar.setImageURI(Uri.fromFile(File(pet.photoPath)))
        } else {
            holder.petAvatar.setImageResource(R.drawable.ic_pet_placeholder)
        }

        holder.itemView.setOnClickListener {
            onItemClick(pet)
        }

        onItemLongClick?.let { longClickListener ->
            holder.itemView.setOnLongClickListener {
                longClickListener(pet)
                true
            }
        }
    }

    override fun getItemCount(): Int = pets.size
}