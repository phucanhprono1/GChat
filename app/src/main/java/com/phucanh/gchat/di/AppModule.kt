package com.phucanh.gchat.di

import android.app.Application
import android.content.Context
import androidx.room.Room
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
//    @Provides
//    fun provideAppDatabase(context: Context): AppDatabase {
//        return return Room.databaseBuilder(
//            context.applicationContext,
//            AppDatabase::class.java, "gchat.db"
//        ).allowMainThreadQueries()
//            .build()
//    }
//    fun provideFriendDao(appDatabase: AppDatabase): FriendDao {
//        return appDatabase.friendDao()
//    }
}