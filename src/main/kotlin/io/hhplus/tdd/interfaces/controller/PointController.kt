package io.hhplus.tdd.interfaces.controller

import io.hhplus.tdd.infra.database.PointHistoryTable
import io.hhplus.tdd.infra.database.UserPointTable
import io.hhplus.tdd.domain.point.PointHistory
import io.hhplus.tdd.domain.point.TransactionType
import io.hhplus.tdd.application.service.PointService
import io.hhplus.tdd.domain.point.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val userPointTable: UserPointTable = UserPointTable()
    private val pointHistoryTable: PointHistoryTable = PointHistoryTable()
    private val pointService: PointService = PointService(userPointTable, pointHistoryTable)

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        return pointService.get(id)
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        return pointService.getHistories(id)
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    suspend fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointService.charge(id, amount, TransactionType.CHARGE)
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    suspend fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        return pointService.use(id, amount, TransactionType.USE)
    }
}