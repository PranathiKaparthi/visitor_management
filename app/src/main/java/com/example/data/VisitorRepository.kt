package com.example.data

import kotlinx.coroutines.flow.Flow

class VisitorRepository(private val visitorDao: VisitorDao) {
    val allVisitors: Flow<List<VisitorRecord>> = visitorDao.getAllVisitors()

    fun getVisitorsByHost(hostName: String): Flow<List<VisitorRecord>> {
        return visitorDao.getVisitorsByHost(hostName)
    }

    suspend fun getVisitorById(id: Int): VisitorRecord? {
        return visitorDao.getVisitorById(id)
    }

    suspend fun getVisitorByOtp(otp: String): VisitorRecord? {
        return visitorDao.getVisitorByOtp(otp)
    }

    suspend fun insertVisitor(record: VisitorRecord): Long {
        return visitorDao.insertVisitor(record)
    }

    suspend fun updateVisitor(record: VisitorRecord) {
        visitorDao.updateVisitor(record)
    }

    suspend fun deleteVisitor(record: VisitorRecord) {
        visitorDao.deleteVisitor(record)
    }

    suspend fun deleteAll() {
        visitorDao.deleteAllVisitors()
    }
}
