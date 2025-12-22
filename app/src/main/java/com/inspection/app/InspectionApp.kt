package com.inspection.app

import android.app.Application
import com.inspection.app.data.AppDatabase

class InspectionApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
