package com.hueno.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * One saved color. Only the measured color is stored; names are derived from it when shown, so
 * improvements to the naming rules apply to colors saved earlier.
 */
@Entity(tableName = "captures")
internal data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val red: Int,
    val green: Int,
    val blue: Int,
    @ColumnInfo(name = "from_photo") val fromPhoto: Boolean,
    /** Name of a ConfidenceLevel, or null for photo picks. */
    @ColumnInfo(name = "confidence_level") val confidenceLevel: String?,
    /** Name of a ConfidenceIssue, or null when there was none. */
    @ColumnInfo(name = "confidence_issue") val confidenceIssue: String?,
    val calibrated: Boolean,
    @ColumnInfo(name = "time_millis") val timeMillis: Long,
)

@Dao
internal interface CaptureDao {
    @Query("SELECT * FROM captures ORDER BY time_millis DESC, id DESC")
    fun observeAll(): Flow<List<CaptureEntity>>

    @Insert
    suspend fun insert(entity: CaptureEntity): Long

    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [CaptureEntity::class], version = 1)
internal abstract class HuenoDatabase : RoomDatabase() {
    abstract fun captures(): CaptureDao

    companion object {
        @Volatile
        private var instance: HuenoDatabase? = null

        fun get(context: Context): HuenoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, HuenoDatabase::class.java, "hueno.db")
                .build()
                .also { instance = it }
        }
    }
}
