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
 * backend (true) — both implementations are compiled in and injectable so a
 * soak test only ever requires flipping the flag, per docs/BACKEND_MIGRATION.md.
 * No ViewModel, use case, or Composable needs to change either way, since
 * they only ever depend on the domain interfaces (AuthRepository, etc.).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseImpl: AuthRepositoryImpl,
        restImpl: RestAuthRepositoryImpl,
    ): AuthRepository = if (BuildConfig.USE_REST_BACKEND) restImpl else firebaseImpl

    @Provides
    @Singleton
    fun provideNoteRepository(
        firebaseImpl: NoteRepositoryImpl,
        restImpl: RestNoteRepositoryImpl,
    ): NoteRepository = if (BuildConfig.USE_REST_BACKEND) restImpl else firebaseImpl

    @Provides
    @Singleton
    fun provideTaskRepository(
        firebaseImpl: TaskRepositoryImpl,
        restImpl: RestTaskRepositoryImpl,
    ): TaskRepository = if (BuildConfig.USE_REST_BACKEND) restImpl else firebaseImpl

    @Provides
    @Singleton
    fun provideSubjectRepository(
        firebaseImpl: SubjectRepositoryImpl,
        restImpl: RestSubjectRepositoryImpl,
    ): SubjectRepository = if (BuildConfig.USE_REST_BACKEND) restImpl else firebaseImpl

    @Provides
    @Singleton
    fun providePointsRepository(
        firebaseImpl: PointsRepositoryImpl,
        restImpl: RestPointsRepositoryImpl,
    ): PointsRepository = if (BuildConfig.USE_REST_BACKEND) restImpl else firebaseImpl
}
