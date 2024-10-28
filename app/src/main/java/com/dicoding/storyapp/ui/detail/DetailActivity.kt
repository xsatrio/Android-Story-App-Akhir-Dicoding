package com.dicoding.storyapp.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.databinding.ActivityDetailBinding
import com.dicoding.storyapp.helper.DateUtils

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        val storyName = intent.getStringExtra(EXTRA_STORY_NAME)
        val storyDescription = intent.getStringExtra(EXTRA_STORY_DESCRIPTION)
        val storyDate = intent.getStringExtra(EXTRA_STORY_DATE)
        val storyImageUrl = intent.getStringExtra(EXTRA_STORY_IMAGE_URL)

        observeViewModel(storyId, storyName, storyDescription, storyDate, storyImageUrl)
    }

    private fun observeViewModel(
        storyId: String?,
        storyName: String?,
        storyDescription: String?,
        storyDate: String?,
        storyImageUrl: String?
    ) {
        if (storyId != null) {
            viewModel.getDetailStory(storyId).observe(this) { storyDetail ->
                if (storyDetail != null) {
                    binding.tvDetailName.text = storyName
                    binding.tvDetailDescription.text = storyDescription
                    binding.tvDetailDate.text = storyDate?.let { DateUtils.localizeDate(it) }
                    Glide.with(this)
                        .load(storyImageUrl)
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_error)
                        .into(binding.ivDetailPhoto)

                    binding.ivDetailPhoto.transitionName = "sharedImage"
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
        const val EXTRA_STORY_NAME = "extra_story_name"
        const val EXTRA_STORY_DESCRIPTION = "extra_story_description"
        const val EXTRA_STORY_DATE = "extra_story_date"
        const val EXTRA_STORY_IMAGE_URL = "extra_story_image_url"
    }
}
