package com.inspection.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.inspection.app.data.dao.AddressDao
import com.inspection.app.data.dao.CaseDao
import com.inspection.app.data.dao.PhotoDao
import com.inspection.app.data.entity.Address
import com.inspection.app.data.entity.Case
import com.inspection.app.data.entity.Photo

@Database(
    entities = [Case::class, Address::class, Photo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
    abstract fun addressDao(): AddressDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inspection_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
