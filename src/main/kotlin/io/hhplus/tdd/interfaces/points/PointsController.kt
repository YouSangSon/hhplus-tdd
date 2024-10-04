package io.hhplus.tdd.interfaces.points

import io.hhplus.tdd.infrastructure.points.PointHistoryTable
import io.hhplus.tdd.infrastructure.points.UserPointTable
import io.hhplus.tdd.domain.points.PointHistory
import io.hhplus.tdd.domain.points.TransactionType
import io.hhplus.tdd.domain.points.PointsService
import io.hhplus.tdd.domain.points.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointsController {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val userPointTable: UserPointTable = UserPointTable()
    private val pointHistoryTable: PointHistoryTable = PointHistoryTable()
    private val pointsService: PointsService = PointsService(userPointTable, pointHistoryTable)

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return pointsService.get(id)
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointsService.getHistories(id)
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    suspend fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointsService.charge(id, amount, TransactionType.CHARGE)
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    suspend fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointsService.use(id, amount, TransactionType.USE)
    }
}