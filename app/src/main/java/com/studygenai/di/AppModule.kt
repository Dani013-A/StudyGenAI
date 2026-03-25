package com.studygenai.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studygenai.ai.ocr.OcrEngine
import com.studygenai.data.firebase.StorageService
import com.studygenai.data.repository.AuthRepositoryImpl
import com.studygenai.data.repository.NoteRepositoryImpl
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.domain.repository.NoteRepository
import com.studygenai.utils.ImageUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.studygenai.domain.usecase.note.DeleteNoteUseCase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorageService(
        @ApplicationContext context: Context
    ): StorageService = StorageService(context)

    @Provides
    @Singleton
    fun provideOcrEngine(): OcrEngine = OcrEngine()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun provideNoteRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storageService: StorageService,
        ocrEngine: OcrEngine
    ): NoteRepository = NoteRepositoryImpl(firestore, auth, storageService, ocrEngine)

    @Provides
    @Singleton
    fun provideImageUtils(
        @ApplicationContext context: Context
    ): ImageUtils {
        ImageUtils.init(context)
        return ImageUtils
    }

    @Provides
    @Singleton
    fun provideDeleteNoteUseCase(
        noteRepository: NoteRepository
    ): DeleteNoteUseCase = DeleteNoteUseCase(noteRepository)
}