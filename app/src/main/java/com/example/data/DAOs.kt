package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TapDrillDao {
    @Query("SELECT * FROM tap_drills ORDER BY id ASC")
    fun getAllDrills(): Flow<List<TapDrillEntity>>

    @Query("SELECT * FROM tap_drills WHERE size LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchDrills(query: String): Flow<List<TapDrillEntity>>

    @Query("SELECT count(*) FROM tap_drills")
    suspend fun getDrillsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrills(drills: List<TapDrillEntity>)

    @Query("DELETE FROM tap_drills")
    suspend fun clearDrills()
}

@Dao
interface WeldingMethodDao {
    @Query("SELECT * FROM welding_methods ORDER BY id ASC")
    fun getAllMethods(): Flow<List<WeldingMethodEntity>>

    @Query("SELECT count(*) FROM welding_methods")
    suspend fun getMethodsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethods(methods: List<WeldingMethodEntity>)

    @Query("DELETE FROM welding_methods")
    suspend fun clearMethods()
}

@Dao
interface ElectrodeDao {
    @Query("SELECT * FROM electrodes ORDER BY id ASC")
    fun getAllElectrodes(): Flow<List<ElectrodeEntity>>

    @Query("SELECT count(*) FROM electrodes")
    suspend fun getElectrodesCount(): Int

    @Query("SELECT * FROM electrodes WHERE awsCode LIKE '%' || :query || '%' OR isoCode LIKE '%' || :query || '%'")
    fun searchElectrodes(query: String): Flow<List<ElectrodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElectrodes(electrodes: List<ElectrodeEntity>)
}

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 50")
    fun getHistory(): Flow<List<CalculationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY id DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE category = :category AND referenceId = :referenceId")
    suspend fun deleteBookmark(category: String, referenceId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE category = :category AND referenceId = :referenceId)")
    fun isBookmarked(category: String, referenceId: Int): Flow<Boolean>
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM user_feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)
}
