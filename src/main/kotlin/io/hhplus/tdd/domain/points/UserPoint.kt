package io.hhplus.tdd.domain.points

import io.hhplus.tdd.domain.models.Constants.MAX_POINTS

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    private fun isNegative(point: Long): Boolean {
        return point < 0
    }

    private fun isExceedMaxPoints(point: Long): Boolean {
        return point > MAX_POINTS
    }

    fun charge(amount: Long, updateMillis: Long): UserPoint {
        val updatedPoint = this.point + amount

        if (isNegative(updatedPoint)) {
            throw IllegalStateException("Point cannot be negative")
        }

        if (isExceedMaxPoints(updatedPoint)) {
            throw IllegalStateException("Point cannot exceed $MAX_POINTS")
        }

        return UserPoint(id, updatedPoint, updateMillis)
    }

    fun use(amount: Long, updateMillis: Long): UserPoint {
        val updatedPoint = this.point - amount

        if (updatedPoint < 0) {
            throw IllegalStateException("Point cannot be negative")
        }

        return UserPoint(id, updatedPoint, updateMillis)
    }
}