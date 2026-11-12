package com.studentcompanion.app.core.di

import com.studentcompanion.app.BuildConfig
import com.studentcompanion.app.data.repository.AuthRepositoryImpl
import com.studentcompanion.app.data.repository.NoteRepositoryImpl
import com.studentcompanion.app.data.repository.PointsRepositoryImpl
import com.studentcompanion.app.data.repository.SubjectRepositoryImpl
import com.studentcompanion.app.data.repository.TaskRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestAuthRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestNoteRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestPointsRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestSubjectRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestTaskRepositoryImpl
import com.studentcompanion.app.domain.repository.AuthRepository
import com.studentcompanion.app.domain.repository.NoteRepository
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.repository.SubjectRepository
import com.studentcompanion.app.domain.repository.TaskRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository interface to its backing implementation.
 *
 * *** THIS IS THE MIGRATION SEAM ***
 * [BuildConfig.USE_REST_BACKEND] picks Firebase (false, default) or the REST
 * backend (true) — both implementations are compiled in so a soak test only
 * ever requires flipping the flag, per docs/BACKEND_MIGRATION.md. Both sides
 * are injected as [Lazy] and only the picked one's `.get()` is called, so
 * only the active backend's dependency graph (OkHttp clients, DataStore,
 * etc.) actually gets constructed — not both.
 * No ViewModel, use case, or Composable needs to change either way, since
 * they only ever depend on the domain interfaces (AuthRepository, etc.).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseImpl: Lazy<AuthRepositoryImpl>,
        restImpl: Lazy<RestAuthRepositoryImpl>,
    ): AuthRepository = if (BuildConfig.USE_REST_BACKEND) restImpl.get() else firebaseImpl.get()

    @Provides
    @Singleton
    fun provideNoteRepository(
        firebaseImpl: Lazy<NoteRepositoryImpl>,
        restImpl: Lazy<RestNoteRepositoryImpl>,
    ): NoteRepository = if (BuildConfig.USE_REST_BACKEND) restImpl.get() else firebaseImpl.get()

    @Provides
    @Singleton
    fun provideTaskRepository(
        firebaseImpl: Lazy<TaskRepositoryImpl>,
        restImpl: Lazy<RestTaskRepositoryImpl>,
    ): TaskRepository = if (BuildConfig.USE_REST_BACKEND) restImpl.get() else firebaseImpl.get()

    @Provides
    @Singleton
    fun provideSubjectRepository(
        firebaseImpl: Lazy<SubjectRepositoryImpl>,
        restImpl: Lazy<RestSubjectRepositoryImpl>,
    ): SubjectRepository = if (BuildConfig.USE_REST_BACKEND) restImpl.get() else firebaseImpl.get()

    @Provides
    @Singleton
    fun providePointsRepository(
        firebaseImpl: Lazy<PointsRepositoryImpl>,
        restImpl: Lazy<RestPointsRepositoryImpl>,
    ): PointsRepository = if (BuildConfig.USE_REST_BACKEND) restImpl.get() else firebaseImpl.get()
}
