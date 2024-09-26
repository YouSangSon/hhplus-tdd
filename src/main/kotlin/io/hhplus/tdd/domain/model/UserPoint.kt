package io.hhplus.tdd.domain.model

import io.hhplus.tdd.domain.model.Constants.MAX_POINTS

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    fun isNegative(): Boolean {
        return point < 0
    }

    fun isExceedMaxPoints(): Boolean {
        return point > MAX_POINTS
    }

    fun charge(amount: Long, updateMillis: Long): UserPoint {
        return UserPoint(id = id, point = point + amount, updateMillis = updateMillis)
    }

    fun use(amount: Long, updateMillis: Long): UserPoint {
        return UserPoint(id = id, point = point - amount, updateMillis = updateMillis)
    }
}