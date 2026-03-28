package com.studygenai

import android.app.Application
import com.studygenai.utils.ImageUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudyGenAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageUtils.init(this)
    }
}
