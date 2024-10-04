package io.hhplus.tdd.point

import io.hhplus.tdd.infrastructure.points.PointHistoryTable
import io.hhplus.tdd.infrastructure.points.UserPointTable
import io.hhplus.tdd.application.common.BusinessException
import io.hhplus.tdd.domain.models.Constants
import io.hhplus.tdd.domain.points.PointsService
import io.hhplus.tdd.domain.points.PointHistory
import io.hhplus.tdd.domain.points.TransactionType
import io.hhplus.tdd.domain.points.UserPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class PointsServiceTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointsService: PointsService

    @BeforeEach
    fun setUp() {
        userPointTable = mock(UserPointTable::class.java)
        pointHistoryTable = mock(PointHistoryTable::class.java)
        pointsService = PointsService(userPointTable, pointHistoryTable)
    }

    @Test
    fun `사용자의 포인트 조회 검증 - UserPoint`() {
        val userId = 1L
        val expectedUserPoint = UserPoint(userId, 100, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(expectedUserPoint)

        val result = pointsService.get(userId)

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

        val result = pointsService.getHistories(userId)

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

        val result = pointsService.charge(userId, chargeAmount, TransactionType.CHARGE)

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
            pointsService.charge(userId, chargeAmount, TransactionType.CHARGE)
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
            pointsService.charge(userId, chargeAmount, TransactionType.CHARGE)
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

        val result = pointsService.use(userId, useAmount, TransactionType.USE)

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
            pointsService.use(userId, useAmount, TransactionType.USE)
        }

        assertEquals("400", exception.code)
        assertEquals("Point cannot be negative", exception.message)
    }
}