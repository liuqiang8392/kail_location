package com.kail.location.views.roothide

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.kail.location.views.base.BaseActivity
import com.kail.location.views.theme.locationTheme
import com.kail.location.viewmodels.RootAppHideViewModel

class RootAppHideActivity : BaseActivity() {
    private val viewModel: RootAppHideViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            locationTheme {
                RootAppHideScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
