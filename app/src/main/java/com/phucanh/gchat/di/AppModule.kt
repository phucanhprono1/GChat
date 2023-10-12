package com.phucanh.gchat.di

import android.app.Application
import android.content.Context
import com.phucanh.gchat.ui.fragments.userprofile.UserProfileViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
    @Provides
    fun provideUserProfileViewModelFactory(context: Context): UserProfileViewModelFactory {
        return UserProfileViewModelFactory(context)
    }
}