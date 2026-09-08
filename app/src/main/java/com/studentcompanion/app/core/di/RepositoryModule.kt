package com.studentcompanion.app.core.di

import com.studentcompanion.app.data.repository.AuthRepositoryImpl
import com.studentcompanion.app.data.repository.NoteRepositoryImpl
import com.studentcompanion.app.data.repository.PointsRepositoryImpl
import com.studentcompanion.app.data.repository.SubjectRepositoryImpl
import com.studentcompanion.app.data.repository.TaskRepositoryImpl
import com.studentcompanion.app.domain.repository.AuthRepository
import com.studentcompanion.app.domain.repository.NoteRepository
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.repository.SubjectRepository
import com.studentcompanion.app.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository interface to its current (Firebase) implementation.
 *
 * *** THIS IS THE MIGRATION SEAM ***
 * When moving to REST APIs later, create e.g. RestAuthRepositoryImpl and swap the
 * @Binds target below — no ViewModel, use case, or Composable needs to change,
 * since they only ever depend on the domain interfaces (AuthRepository, etc.).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindPointsRepository(impl: PointsRepositoryImpl): PointsRepository
}
