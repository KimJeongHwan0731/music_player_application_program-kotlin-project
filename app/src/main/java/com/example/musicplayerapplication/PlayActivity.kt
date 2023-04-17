package com.example.musicplayerapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayerapplication.databinding.ActivityPlayBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlayActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityPlayBinding
    val ALBUM_IMAGE_SIZE = 90
    var mediaPlayer: MediaPlayer? = null
    lateinit var musicData: MusicData
    private var playList: MutableList<Parcelable>? = null
    private var currentposition: Int = 0
    var mp3playerJob: Job? = null
    var pauseFlag = false
    var shuffleFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playList = intent.getParcelableArrayListExtra("parcelableList")
        currentposition = intent.getIntExtra("position", 0)
        musicData = playList?.get(currentposition) as MusicData

        binding.albumTitle.text = musicData.title
        binding.albumArtist.text = musicData.artist
        binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
        binding.tvPlayDuration.text = "00:00"
        val bitmap = musicData.getAlbumBitmap(this, ALBUM_IMAGE_SIZE)
        if (bitmap != null) {
            binding.ivPlayerAlbum.setImageBitmap(bitmap)
        } else {
            binding.ivPlayerAlbum.setImageResource(R.drawable.music)
        }

        mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())

        binding.ivPlay.setOnClickListener(this)
        binding.ivNext.setOnClickListener(this)
        binding.ivPrevious.setOnClickListener(this)
        binding.ivShuffle.setOnClickListener(this)
        binding.ivRepeat.setOnClickListener(this)
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivPlay -> {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    binding.ivPlay.setImageResource(R.drawable.play)
                    pauseFlag = true
                } else {
                    mediaPlayer?.start()
                    binding.ivPlay.setImageResource(R.drawable.pause)
                    pauseFlag = false
                    getCoroutine()
                }
            }

            R.id.ivNext -> {
                if (currentposition < playList!!.size - 1) {
                    currentposition++
                } else {
                    currentposition = 0
                }
                getMusic()
                mediaPlayer?.start()
                if (shuffleFlag) {
                    mediaPlayer?.setOnCompletionListener {
                        playRandom()
                    }
                } else {
                    mediaPlayer?.setOnCompletionListener(null)
                }
                getCoroutine()

                if (shuffleFlag) {
                    playRandom()
                }
            }

            R.id.ivPrevious -> {
                if (currentposition > 0) {
                    currentposition--
                } else {
                    currentposition = playList!!.size - 1
                }
                getMusic()
                mediaPlayer?.start()
                if (shuffleFlag) {
                    mediaPlayer?.setOnCompletionListener {
                        playRandom()
                    }
                } else {
                    mediaPlayer?.setOnCompletionListener(null)
                }
                getCoroutine()

                if (shuffleFlag) {
                    playRandom()
                }
            }

            R.id.ivShuffle -> {
                shuffleFlag = !shuffleFlag
                if (shuffleFlag) {
                    Toast.makeText(applicationContext, "셔플을 사용합니다.", Toast.LENGTH_SHORT).show()
                    mediaPlayer?.setOnCompletionListener {
                        playRandom()
                    }
                } else {
                    Toast.makeText(applicationContext, "셔플을 사용하지 않습니다.", Toast.LENGTH_SHORT).show()
                    mediaPlayer?.setOnCompletionListener(null)
                }
            }

            R.id.ivRepeat -> {
                Toast.makeText(applicationContext, "현재 음악을 반복합니다.", Toast.LENGTH_SHORT).show()
                mediaPlayer?.setOnCompletionListener {
                    mediaPlayer?.seekTo(0)
                    mediaPlayer?.start()
                }
            }
        }
    }

    private fun playRandom() {
        val lastSong = playList!!.size - 1
        var nextSong = (0 until lastSong).random()
        if (nextSong >= currentposition) {
            nextSong++
        }
        currentposition = nextSong % playList!!.size
        mediaPlayer?.reset()
        musicData = playList!!.get(currentposition) as MusicData
        mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())

        val bitmap = musicData.getAlbumBitmap(this, ALBUM_IMAGE_SIZE)
        runOnUiThread {
            binding.albumTitle.text = musicData.title
            binding.albumArtist.text = musicData.artist
            binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
            if (bitmap != null) {
                binding.ivPlayerAlbum.setImageBitmap(bitmap)
            } else {
                binding.ivPlayerAlbum.setImageResource(R.drawable.music)
            }
            binding.ivPlay.setImageResource(R.drawable.pause)
            binding.seekBar.max = mediaPlayer!!.duration
            pauseFlag = false
            getCoroutine()
        }
    }

    private fun getCoroutine() {
        mediaPlayer?.start()
        binding.ivPlay.setImageResource(R.drawable.pause)

        mediaPlayer?.setOnCompletionListener {
            if (currentposition < playList!!.size - 1) {
                currentposition++
            } else {
                currentposition = 0
            }
            mediaPlayer?.reset()
            musicData = playList!!.get(currentposition) as MusicData
            mediaPlayer = MediaPlayer.create(this@PlayActivity, musicData.getMusicUri())
            mediaPlayer?.start()

            val bitmap = musicData.getAlbumBitmap(this@PlayActivity, ALBUM_IMAGE_SIZE)
            runOnUiThread {
                binding.albumTitle.text = musicData.title
                binding.albumArtist.text = musicData.artist
                binding.tvTotalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
                if (bitmap != null) {
                    binding.ivPlayerAlbum.setImageBitmap(bitmap)
                } else {
                    binding.ivPlayerAlbum.setImageResource(R.drawable.music)
                }
                binding.ivPlay.setImageResource(R.drawable.pause)
                binding.seekBar.max = mediaPlayer!!.duration
                binding.seekBar.progress = 0
                binding.tvPlayDuration.text = "00:00"
            }
            getCoroutine()
        }

        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
        mp3playerJob = backgroundScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                var currentPosition = mediaPlayer?.currentPosition!!
                runOnUiThread {
                    binding.seekBar.progress = currentPosition
                    binding.tvPlayDuration.text =
                        SimpleDateFormat("mm:ss").format(mediaPlayer?.currentPosition)
                }
                try {
                    delay(1000)
                } catch (e: Exception) {
                    Log.e("PlayActivity", "delay 오류발생 ${e.printStackTrace()}")
                }
            }
        }
    }

    override fun onBackPressed() {
        mediaPlayer?.stop()
        mp3playerJob?.cancel()
        mediaPlayer = null
        finish()
    }

    fun getMusic() {
        mediaPlayer?.stop()
        musicData = playList!!.get(currentposition) as MusicData
        mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())
        binding.seekBar.progress = 0
        binding.tvPlayDuration.text = "00:00"
        binding.seekBar.max = mediaPlayer?.duration ?: 0
        binding.tvTotalDuration.text =
            SimpleDateFormat("mm:ss").format(musicData.duration)
        binding.albumTitle.text = musicData.title
        binding.albumArtist.text = musicData.artist
        binding.ivPlay.setImageResource(R.drawable.pause)
        val bitmap = musicData.getAlbumBitmap(this, ALBUM_IMAGE_SIZE)
        if (bitmap != null) {
            binding.ivPlayerAlbum.setImageBitmap(bitmap)
        } else {
            binding.ivPlayerAlbum.setImageResource(R.drawable.music)
        }
        mediaPlayer?.start()
    }
}