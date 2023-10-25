package com.phucanh.gchat.di

import android.app.Application
import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.phucanh.gchat.viewModels.UserProfileViewModelFactory
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
    @Provides
    fun provideStorageReference(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }
}