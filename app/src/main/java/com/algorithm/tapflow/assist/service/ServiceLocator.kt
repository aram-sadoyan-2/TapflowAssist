package com.algorithm.tapflow.assist.service

import android.content.Context
import androidx.room.Room
import com.algorithm.tapflow.assist.data.db.TapFlowDatabase
import com.algorithm.tapflow.assist.data.repository.PresetRepository
import com.algorithm.tapflow.assist.data.repository.PresetRepositoryImpl
import com.algorithm.tapflow.assist.domain.session.SessionController

object ServiceLocator {

    @Volatile
    private var database: TapFlowDatabase? = null

    @Volatile
    private var repository: PresetRepository? = null

    @Volatile
    private var sessionController: SessionController? = null

    fun provideDatabase(context: Context): TapFlowDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                TapFlowDatabase::class.java,
                "tapflow_db"
            ).build().also { database = it }
        }
    }

    fun providePresetRepository(context: Context): PresetRepository {
        return repository ?: synchronized(this) {
            repository ?: PresetRepositoryImpl(
                provideDatabase(context).presetDao()
            ).also { repository = it }
        }
    }

    fun provideSessionController(): SessionController {
        return sessionController ?: synchronized(this) {
            sessionController ?: SessionController().also { sessionController = it }
        }
    }
}