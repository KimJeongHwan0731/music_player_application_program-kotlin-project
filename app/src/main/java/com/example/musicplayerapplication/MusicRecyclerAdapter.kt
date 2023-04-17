package com.example.musicplayerapplication

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapplication.MainActivity.Companion.DB_NAME
import com.example.musicplayerapplication.MainActivity.Companion.VERSION
import com.example.musicplayerapplication.databinding.ItemRecyclerBinding
import java.text.SimpleDateFormat

class MusicRecyclerAdapter(val context: Context, val musicList: MutableList<MusicData>) :
    RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    val ALBUM_IMAGE_SIZE = 280

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding =
            ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val bitmap = musicList.get(position).getAlbumBitmap(context, ALBUM_IMAGE_SIZE)
        if (bitmap != null) {
            binding.ivAlbumImage.setImageBitmap(bitmap)
        } else {
            binding.ivAlbumImage.setImageResource(R.drawable.music)
        }
        binding.tvTitle.text = musicList.get(position).title
        binding.tvTitle.setSelected(true)
        binding.tvArtist.text = musicList.get(position).artist
        binding.tvArtist.setSelected(true)
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(musicList.get(position).duration)
        when (musicList.get(position).likes) {
            0 -> binding.ivItemLike.setImageResource(R.drawable.favorite_unlike_24)
            1 -> binding.ivItemLike.setImageResource(R.drawable.favorite_like_24)
        }

        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context, PlayActivity::class.java)
            val parcelableList: ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>
            intent.putExtra("parcelableList", parcelableList)
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }

        binding.ivItemLike.setOnClickListener {
            when (musicList.get(position).likes) {
                0 -> {
                    musicList.get(position).likes = 1
                    binding.ivItemLike.setImageResource(R.drawable.favorite_like_24)
                    Toast.makeText(context, "좋아요 반영되었습니다.", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    musicList.get(position).likes = 0
                    binding.ivItemLike.setImageResource(R.drawable.favorite_unlike_24)
                    Toast.makeText(context, "좋아요 취소되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            val db = DBOpenHelper(context, DB_NAME, VERSION)
            var errorFlag = db.updateLike(musicList.get(position))
            if (errorFlag) {
                Toast.makeText(context, "좋아요 반영 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                this.notifyDataSetChanged()
            }
        }
    }

    inner class CustomViewHolder(val binding: ItemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)
}