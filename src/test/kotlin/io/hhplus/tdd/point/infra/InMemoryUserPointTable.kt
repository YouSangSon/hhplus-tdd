package io.hhplus.tdd.point.infra

import io.hhplus.tdd.infra.database.UserPointTable
import io.hhplus.tdd.domain.model.UserPoint
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserPointTable : UserPointTable() {
    private val points = ConcurrentHashMap<Long, UserPoint>()

    override fun selectById(id: Long): UserPoint {
        return points.getOrPut(id) { UserPoint(id, 100L, System.currentTimeMillis()) }
    }

    override fun insertOrUpdate(id: Long, amount: Long): UserPoint {
        val updatedUserPoint = UserPoint(id, amount, System.currentTimeMillis())
        points[id] = updatedUserPoint
        return updatedUserPoint
    }
}