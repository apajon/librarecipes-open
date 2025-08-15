package com.apajon.librarecipes.data.local.dao

import androidx.room.*
import com.apajon.librarecipes.data.local.entities.FeedbackExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackExecutionDao {

    @Query("SELECT * FROM feedback_execution WHERE executionId = :executionId")
    fun getFeedbackForExecution(executionId: String): Flow<List<FeedbackExecutionEntity>>

    @Query("SELECT * FROM feedback_execution WHERE conviveId = :conviveId")
    fun getFeedbackForConvive(conviveId: String): Flow<List<FeedbackExecutionEntity>>

    @Query("SELECT * FROM feedback_execution WHERE id = :id")
    suspend fun getFeedback(id: String): FeedbackExecutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackExecutionEntity)

    @Update
    suspend fun updateFeedback(feedback: FeedbackExecutionEntity)

    @Delete
    suspend fun deleteFeedback(feedback: FeedbackExecutionEntity)

    @Query("DELETE FROM feedback_execution WHERE executionId = :executionId")
    suspend fun deleteFeedbackForExecution(executionId: String)
}