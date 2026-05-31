package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TapDrillEntity::class,
        WeldingMethodEntity::class,
        ElectrodeEntity::class,
        CalculationHistoryEntity::class,
        BookmarkEntity::class,
        FeedbackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tapDrillDao(): TapDrillDao
    abstract fun weldingMethodDao(): WeldingMethodDao
    abstract fun electrodeDao(): ElectrodeDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "engineref_pro_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
