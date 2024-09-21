package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class PointControllerTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    // code executed before each test case execution
    fun setUp() {
        userPointTable = mock(UserPointTable::class.java)
        pointHistoryTable = mock(PointHistoryTable::class.java)
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    @Test
    fun `getUserPoint should return user point`() {
        val userId = 1L
        val expectedUserPoint = UserPoint(userId, 100, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(expectedUserPoint)

        val result = pointService.getUserPoint(userId)

        assert(result == expectedUserPoint)
        verify(userPointTable).selectById(userId)
    }

    @Test
    fun `getUserPointHistories should return list of point histories`() {
        val userId = 1L
        val expectedHistories = listOf(
            PointHistory(1L, userId, TransactionType.CHARGE, 50, System.currentTimeMillis()),
            PointHistory(2L, userId, TransactionType.USE, 30, System.currentTimeMillis())
        )

        `when`(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedHistories)

        val result = pointService.getUserPointHistories(userId)

        assert(result == expectedHistories)
        verify(pointHistoryTable).selectAllByUserId(userId)
    }

    @Test
    fun `chargeUserPoint should increase user point`() {
        val userId = 1L
        val initialPoint = 100L
        val chargeAmount = 50L
        val initialTime = System.currentTimeMillis()
        val userPoint = UserPoint(userId, initialPoint, initialTime)

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val result = pointService.useOrChargeUserPoint(userId, chargeAmount, TransactionType.CHARGE)

        assert(result.point == initialPoint + chargeAmount)
        assert(result.updateMillis >= initialTime)
        verify(userPointTable).insertOrUpdate(eq(userId), eq(initialPoint + chargeAmount))
    }

    @Test
    fun `chargeUserPoint should throw exception when initial point is negative`() {
        val userId = 1L
        val initialPoint = -10L
        val chargeAmount = 50L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        assertThrows<IllegalArgumentException> {
            pointService.useOrChargeUserPoint(userId, chargeAmount, TransactionType.CHARGE)
        }
    }

    @Test
    fun `useUserPoint should decrease user point`() {
        val userId = 1L
        val initialPoint = 100L
        val useAmount = 50L
        val initialTime = System.currentTimeMillis()
        val userPoint = UserPoint(userId, initialPoint, initialTime)

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        val result = pointService.useOrChargeUserPoint(userId, useAmount, TransactionType.USE)

        assert(result.point == initialPoint - useAmount)
        assert(result.updateMillis >= initialTime)
        verify(userPointTable).insertOrUpdate(eq(userId), eq(initialPoint - useAmount))
    }

    @Test
    fun `useUserPoint should throw exception when not enough points`() {
        val userId = 1L
        val initialPoint = 30L
        val useAmount = 50L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        assertThrows<IllegalArgumentException> {
            pointService.useOrChargeUserPoint(userId, useAmount, TransactionType.USE)
        }
    }

    @Test
    fun `useUserPoint should throw exception when initial point is negative`() {
        val userId = 1L
        val initialPoint = -10L
        val useAmount = 50L
        val userPoint = UserPoint(userId, initialPoint, System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(userPoint)

        assertThrows<IllegalArgumentException> {
            pointService.useOrChargeUserPoint(userId, useAmount, TransactionType.USE)
        }
    }
}