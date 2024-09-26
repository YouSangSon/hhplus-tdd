package io.hhplus.tdd.point

import io.hhplus.tdd.infra.database.PointHistoryTable
import io.hhplus.tdd.infra.database.UserPointTable
import io.hhplus.tdd.domain.BusinessException
import io.hhplus.tdd.domain.model.Constants
import io.hhplus.tdd.domain.model.PointHistory
import io.hhplus.tdd.domain.model.TransactionType
import io.hhplus.tdd.domain.model.UserPoint
import io.hhplus.tdd.domain.service.PointService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class PointServiceTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        userPointTable = mock(UserPointTable::class.java)
        pointHistoryTable = mock(PointHistoryTable::class.java)
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    @Test
    fun `사용자의 포인트 조회 검증 - UserPoint`() {
        val userId = 1L
        val expectedUserPoint = UserPoint(userId, 100, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(expectedUserPoint)

        val result = pointService.get(userId)

        assertEquals(expectedUserPoint, result)
        verify(userPointTable).selectById(userId)
    }

    @Test
    fun `사용자의 포인트 사용 내역 조회 검증 - List of PointHistory`() {
        val userId = 1L
        val expectedHistories = listOf(
            PointHistory(1L, userId, TransactionType.CHARGE, 50, System.currentTimeMillis()),
            PointHistory(2L, userId, TransactionType.USE, 30, System.currentTimeMillis())
        )

        `when`(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedHistories)

        val result = pointService.getHistories(userId)

        assertEquals(expectedHistories, result)
        verify(pointHistoryTable).selectAllByUserId(userId)
    }

    @Test
    fun `사용자의 포인트 충전 기능 검증 - UserPoint`() {
        val userId = 1L
        val initialPoint = 100L
        val chargeAmount = 50L
        val initialTime = System.currentTimeMillis()
        val userPoint = UserPoint(userId, initialPoint, initialTime)

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val result = pointService.charge(userId, chargeAmount, TransactionType.CHARGE)

        assertEquals(initialPoint + chargeAmount, result.point)
    }

    @Test
    fun `사용자의 포인트 충전 시 음수 포인트 예외 처리 검증 - BusinessException`() {
        val userId = 1L
        val initialPoint = -60L
        val chargeAmount = 50L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val exception = assertThrows<BusinessException> {
            pointService.charge(userId, chargeAmount, TransactionType.CHARGE)
        }

        assertEquals("400", exception.code)
        assertEquals("Point cannot be negative", exception.message)
    }

    @Test
    fun `사용자의 포인트 충전 시 최대 포인트 초과 예외 처리 검증 - BusinessException`() {
        val userId = 1L
        val initialPoint = Constants.MAX_POINTS
        val chargeAmount = 1000L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val exception = assertThrows<BusinessException> {
            pointService.charge(userId, chargeAmount, TransactionType.CHARGE)
        }

        assertEquals("400", exception.code)
        assertEquals("Point cannot exceed ${Constants.MAX_POINTS}", exception.message)
    }

    @Test
    fun `사용자의 포인트 사용 기능 검증 - UserPoint`() {
        val userId = 1L
        val initialPoint = 100L
        val useAmount = 50L
        val initialTime = System.currentTimeMillis()
        val userPoint = UserPoint(userId, initialPoint, initialTime)

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val result = pointService.use(userId, useAmount, TransactionType.USE)

        assertEquals(initialPoint - useAmount, result.point)
    }

    @Test
    fun `사용자가 포인트 사용할 때 부족 시 예외 처리 - BusinessException`() {
        val userId = 1L
        val initialPoint = 30L
        val useAmount = 50L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val exception = assertThrows<BusinessException> {
            pointService.use(userId, useAmount, TransactionType.USE)
        }

        assertEquals("400", exception.code)
        assertEquals("Point cannot be negative", exception.message)
    }
}