package com.codex.animestream.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val episodeId: String,
    val animeId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)

@Entity(tableName = "library")
data class LibraryEntity(
    @PrimaryKey val animeId: String,
    val title: String,
    val posterUrl: String,
    val kind: String,
    val updatedAt: Long,
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE episodeId = :episodeId")
    fun observe(episodeId: String): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress ORDER BY updatedAt DESC LIMIT 30")
    fun continueWatching(): Flow<List<ProgressEntity>>

    @Upsert
    suspend fun upsert(entity: ProgressEntity)
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library WHERE kind = :kind ORDER BY updatedAt DESC")
    fun observeKind(kind: String): Flow<List<LibraryEntity>>

    @Upsert
    suspend fun upsert(entity: LibraryEntity)

    @Query("DELETE FROM library WHERE animeId = :animeId AND kind = :kind")
    suspend fun remove(animeId: String, kind: String)
}

@Database(entities = [ProgressEntity::class, LibraryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun libraryDao(): LibraryDao
}
