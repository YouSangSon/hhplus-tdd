package io.hhplus.tdd.domain.points

import io.hhplus.tdd.infrastructure.points.PointHistoryTable
import io.hhplus.tdd.infrastructure.points.UserPointTable
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Service
class PointsService(
    private val userPointTable: UserPointTable, private val pointHistoryTable: PointHistoryTable
) {
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    fun charge(id: Long, amount: Long, transactionType: TransactionType): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }

        lock.lock()

        try {
            val userPoint = userPointTable.selectById(id)
            val updatedUserPoint = userPoint.charge(amount, System.currentTimeMillis())

            userPointTable.insertOrUpdate(id, updatedUserPoint.point)
            pointHistoryTable.insert(id, updatedUserPoint.point, transactionType, System.currentTimeMillis())

            return updatedUserPoint
        } finally { // 자원 해제의 보장을 위해
            lock.unlock()
        }
    }

    fun use(id: Long, amount: Long, transactionType: TransactionType): UserPoint {
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }

        lock.lock()

        try {
            val userPoint = userPointTable.selectById(id)
            val updatedUserPoint = userPoint.use(amount, System.currentTimeMillis())

            userPointTable.insertOrUpdate(id, updatedUserPoint.point)
            pointHistoryTable.insert(id, updatedUserPoint.point, transactionType, System.currentTimeMillis())

            return updatedUserPoint
        } finally {
            lock.unlock()
        }
    }

    fun get(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }
}