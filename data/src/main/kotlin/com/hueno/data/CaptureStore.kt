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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    /** A name the user gave this color, such as "Living room wall". */
    val label: String? = null,
    /** The collection this color is filed under; null when it is in none. */
    val collection: String? = null,
)

@Dao
internal interface CaptureDao {
    @Query("SELECT * FROM captures ORDER BY time_millis DESC, id DESC")
    fun observeAll(): Flow<List<CaptureEntity>>

    @Insert
    suspend fun insert(entity: CaptureEntity): Long

    @Query("DELETE FROM captures WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE captures SET label = :label WHERE id = :id")
    suspend fun setLabel(id: Long, label: String?)

    @Query("UPDATE captures SET collection = :collection WHERE id = :id")
    suspend fun setCollection(id: Long, collection: String?)
}

@Database(entities = [CaptureEntity::class], version = 2)
internal abstract class HuenoDatabase : RoomDatabase() {
    abstract fun captures(): CaptureDao

    companion object {
        /** Version 2 adds the user's own name for a color and the collection it belongs to. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE captures ADD COLUMN label TEXT")
                db.execSQL("ALTER TABLE captures ADD COLUMN collection TEXT")
            }
        }

        @Volatile
        private var instance: HuenoDatabase? = null

        fun get(context: Context): HuenoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, HuenoDatabase::class.java, "hueno.db")
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}
