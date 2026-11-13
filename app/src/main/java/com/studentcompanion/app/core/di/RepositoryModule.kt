package com.studentcompanion.app.core.di

import com.studentcompanion.app.data.repository.rest.RestAuthRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestNoteRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestParentLinkRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestPointsRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestSubjectRepositoryImpl
import com.studentcompanion.app.data.repository.rest.RestTaskRepositoryImpl
import com.studentcompanion.app.domain.repository.AuthRepository
import com.studentcompanion.app.domain.repository.NoteRepository
import com.studentcompanion.app.domain.repository.ParentLinkRepository
import com.studentcompanion.app.domain.repository.PointsRepository
import com.studentcompanion.app.domain.repository.SubjectRepository
import com.studentcompanion.app.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository interface to its implementation against the
 * NestJS backend. ViewModels, use cases and Composables depend only on the
 * domain interfaces, so swapping a backend means changing only this file.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RestAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: RestNoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: RestTaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(impl: RestSubjectRepositoryImpl): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindPointsRepository(impl: RestPointsRepositoryImpl): PointsRepository

    @Binds
    @Singleton
    abstract fun bindParentLinkRepository(impl: RestParentLinkRepositoryImpl): ParentLinkRepository
}
