package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(private val userPointTable: UserPointTable, private val pointHistoryTable: PointHistoryTable) {
    fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    fun useOrChargeUserPoint(id: Long, amount: Long, type: TransactionType): UserPoint {
        val userPoint = userPointTable.selectById(id)
        if (userPoint.point < 0) {
            throw IllegalArgumentException("you have a point that can't exist")
        }

        val updatedUserPoint = when (type) {
            TransactionType.CHARGE -> {
                val updatedUserPoint = userPoint.copy(point = userPoint.point + amount)
                updatedUserPoint
            }
            TransactionType.USE -> {
                if (userPoint.point < amount) {
                    throw IllegalArgumentException("you don't have enough point")
                }
                val updatedUserPoint = userPoint.copy(point = userPoint.point - amount)
                updatedUserPoint
            }
        }

        userPointTable.insertOrUpdate(id, updatedUserPoint.point)

        return updatedUserPoint
    }
}