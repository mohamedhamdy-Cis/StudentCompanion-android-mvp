package com.studentcompanion.app.data.remote.rest

import com.studentcompanion.app.data.remote.rest.dto.AuthResponse
import com.studentcompanion.app.data.remote.rest.dto.AwardPointsRequest
import com.studentcompanion.app.data.remote.rest.dto.CompleteTaskRequest
import com.studentcompanion.app.data.remote.rest.dto.CreateNoteRequest
import com.studentcompanion.app.data.remote.rest.dto.CreateSubjectRequest
import com.studentcompanion.app.data.remote.rest.dto.LoginRequest
import com.studentcompanion.app.data.remote.rest.dto.PointsSummaryResponse
import com.studentcompanion.app.data.remote.rest.dto.PresignUploadRequest
import com.studentcompanion.app.data.remote.rest.dto.PresignUploadResponse
import com.studentcompanion.app.data.remote.rest.dto.RefreshRequest
import com.studentcompanion.app.data.remote.rest.dto.RegisterRequest
import com.studentcompanion.app.data.remote.rest.dto.RestNoteDto
import com.studentcompanion.app.data.remote.rest.dto.RestSubjectDto
import com.studentcompanion.app.data.remote.rest.dto.RestTaskDto
import com.studentcompanion.app.data.remote.rest.dto.RestUserDto
import com.studentcompanion.app.data.remote.rest.dto.SubmitHomeworkRequest
import com.studentcompanion.app.data.remote.rest.dto.TokenPairResponse
import com.studentcompanion.app.data.remote.rest.dto.UpdateNoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Mirrors the backend's REST contract 1:1 — see docs/API_CONTRACT.md in the backend repo. */
interface StudentCompanionApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): RestUserDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenPairResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequest)

    @GET("subjects")
    suspend fun getSubjects(): List<RestSubjectDto>

    @POST("subjects")
    suspend fun createSubject(@Body body: CreateSubjectRequest): RestSubjectDto

    @GET("notes")
    suspend fun getNotes(@Query("subjectId") subjectId: String?): List<RestNoteDto>

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: String): RestNoteDto

    @POST("notes")
    suspend fun createNote(@Body body: CreateNoteRequest): RestNoteDto

    @PATCH("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body body: UpdateNoteRequest): RestNoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)

    @GET("tasks")
    suspend fun getTasks(): List<RestTaskDto>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): RestTaskDto

    @POST("tasks/ensure-prayers")
    suspend fun ensureTodaysPrayerTasks(): List<RestTaskDto>

    @PATCH("tasks/{id}/complete")
    suspend fun completeTask(@Path("id") id: String, @Body body: CompleteTaskRequest): RestTaskDto

    @POST("tasks/{id}/attachments/presign-upload")
    suspend fun presignUpload(
        @Path("id") taskId: String,
        @Body body: PresignUploadRequest,
    ): PresignUploadResponse

    @POST("tasks/{id}/submit")
    suspend fun submitHomework(
        @Path("id") id: String,
        @Body body: SubmitHomeworkRequest,
    ): RestTaskDto

    @GET("points/summary")
    suspend fun getPointsSummary(): PointsSummaryResponse

    @POST("points/award")
    suspend fun awardPoints(@Body body: AwardPointsRequest)
}
