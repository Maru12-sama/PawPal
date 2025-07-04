package com.example.pawpal.mainMenu.guides

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pawpal.R

class GuideAdapter(
    private val items: List<GuideItem>,
    private val navController: NavController
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    companion object {
        private const val TAG = "GuideAdapter"
    }

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageBackground)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guide, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(item.imageResId)
        holder.titleText.text = item.title
        holder.dateText.text = item.date

        holder.itemView.setOnClickListener {
            Log.d(TAG, "Item clicked: ${item.title}")

            val bundle = Bundle().apply {
                putString("title", item.title)
                putString("date", item.date)
                putString("source", item.source)
                putString("description", getDescription(item.title))
                putInt("imageRes", item.imageResId)
            }

            Log.d(TAG, "Bundle created with title: ${bundle.getString("title")}")
            Log.d(TAG, "NavController: $navController")

            try {
                navController.navigate(R.id.action_homeFragment_to_guideDetailFragment, bundle)
                Log.d(TAG, "Navigation successful")
            } catch (e: Exception) {
                Log.e(TAG, "Navigation failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun getDescription(title: String): String {
        return when (title) {
            "Apa Saja Makanan yang Menjadi Racun bagi Anjing" -> "Hindari makanan seperti cokelat, bawang, dan anggur yang berbahaya bagi anjing."
            "4 Kebiasaan Makan Kucing yang Tak Biasa" -> "Kucing kadang makan rumput atau tidur di dekat makanan mereka."
            "Why They Confuse You About What to Feed" -> "Banyak mitos tentang makanan hewan peliharaan yang perlu diperiksa."
            "Dog Grooming: 10 Beginner MISTAKES" -> "Hindari memotong terlalu dekat kulit dan gunakan alat yang tepat."
            "The top five dog-grooming tips from pro" -> "Sikat bulu secara rutin dan perhatikan telinga anjing."
            "Tips for brushing your cat" -> "Gunakan sikat lembut dan lakukan secara perlahan."
            else -> "Deskripsi tidak tersedia."
        }
    }
}