package com.lumeai.banking.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val creditScore: Int = 700,
    val monthlyIncome: Double = 60000.0,
    val monthlyDebt: Double = 20000.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "improvement_steps")
data class ImprovementStepEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val weight: Double,
    val status: String // Pending | Done
)

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id=1")
    fun observe(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)
}

@Dao
interface StepsDao {
    @Query("SELECT * FROM improvement_steps")
    fun observeAll(): Flow<List<ImprovementStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ImprovementStepEntity>)

    @Query("DELETE FROM improvement_steps")
    suspend fun clear()
}

@Database(
    entities = [UserProfileEntity::class, ImprovementStepEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lumeai.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = inst
                inst
            }
        }
    }
}


