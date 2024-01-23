package com.phucanh.gchat.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.phucanh.gchat.room.AppDatabase
import com.phucanh.gchat.room.FriendDao
import com.phucanh.gchat.room.GroupDao
import com.phucanh.gchat.viewModels.ChatViewModel

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideStorageReference(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    @Provides
    fun providesUserReference(): DatabaseReference {
        return FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users")
    }

    @Provides
    fun provideFriendReference(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://gchat-af243-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }

    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "gchat.db"
        ).allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideFriendDao(appDatabase: AppDatabase): FriendDao {
        return appDatabase.friendDao()
    }

    @Provides
    fun provideGroupDao(appDatabase: AppDatabase): GroupDao {
        return appDatabase.groupDao()
    }

    @Provides
    fun provideChatViewModel(userReference: DatabaseReference, firebaseDatabase: FirebaseDatabase, application: Application): ChatViewModel {
        return ChatViewModel(userReference, firebaseDatabase, application)
    }
}
