package com.abizer_r.minitruecaller.di

import android.content.Context
import com.abizer_r.minitruecaller.data.repository.CallerRepositoryImpl
import com.abizer_r.minitruecaller.domain.repository.CallerRepository
import com.abizer_r.minitruecaller.domain.usecase.GetCallerInfoUseCase
import com.abizer_r.minitruecaller.domain.usecase.SaveCallerInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideCallerRepository(): CallerRepository {
        return CallerRepositoryImpl()
    }

    @Provides
    fun provideGetCallerInfoUseCase(repo: CallerRepository): GetCallerInfoUseCase {
        return GetCallerInfoUseCase(repo)
    }

    @Provides
    fun provideSaveCallerInfoUseCase(repo: CallerRepository): SaveCallerInfoUseCase {
        return SaveCallerInfoUseCase(repo)
    }
}
