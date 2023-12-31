package com.code.presubmission.view.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.code.presubmission.data.response.DetailStoryResponse
import com.code.presubmission.databinding.ActivityDetailBinding
import com.code.presubmission.view.ViewModelFactory
import com.code.presubmission.view.welcome.WelcomeActivity

class DetailActivity : AppCompatActivity() {
    private val detailViewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        detailViewModel.getSession().observe(this){user ->
            if(!user.isLogin){
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            val detailStory = intent.getStringExtra(EXTRA_DATA)
            if(detailStory != null){
                detailViewModel.getDetail(user.token,detailStory).observe(this){result ->
                    Log.e("hasil", "setDetailStory: ${result}")
                    setDataDetail(result)
                    showLoading(false)
                }
            }
        }
        detailViewModel.Loading.observe(this){
            showLoading(it)
        }
    }


    private fun showLoading(isLoading: Boolean){
        binding.progress.visibility =if(isLoading) View.VISIBLE else View.GONE
    }
    private fun setDataDetail(detail: DetailStoryResponse){
        binding.apply {
            tvJudulDetail.text = detail.story?.name
            tvDeskripsiDetail.text = detail.story?.description
            Glide.with(this@DetailActivity)
                .load(detail.story?.photoUrl)
                .into(ivStory)
        }
    }

    companion object{
        const val EXTRA_DATA = "extra"
    }
}