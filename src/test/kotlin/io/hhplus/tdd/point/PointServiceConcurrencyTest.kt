package io.hhplus.tdd.point

import io.hhplus.tdd.infra.database.PointHistoryTable
import io.hhplus.tdd.domain.model.TransactionType
import io.hhplus.tdd.domain.model.UserPoint
import io.hhplus.tdd.domain.service.PointService
import io.hhplus.tdd.point.infra.InMemoryUserPointTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PointServiceConcurrencyTest {
    private lateinit var userPointTable: InMemoryUserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        userPointTable = InMemoryUserPointTable()
        pointHistoryTable = mock(PointHistoryTable::class.java)
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    @Test
    fun `여러 스레드가 순차적으로 포인트를 충전해야 한다`() {
        val userId = 1L
        val amountToCharge = 50L
        val transactionType = TransactionType.CHARGE

        val pointService = PointService(userPointTable, pointHistoryTable) // 실제 객체로 서비스 생성

        val numThreads = 10
        val latch = CountDownLatch(numThreads)
        val executor = Executors.newFixedThreadPool(numThreads)

        repeat(numThreads) {
            executor.submit {
                pointService.charge(userId, amountToCharge, transactionType)
                latch.countDown()
            }
        }

        latch.await()

        // 최종 포인트는 100 + 50 * 10 = 600이어야 함
        val expectedFinalPoint = 100L + (amountToCharge * numThreads)
        val actualFinalPoint = pointService.get(userId).point

        assertEquals(expectedFinalPoint, actualFinalPoint)
    }

    @Test
    fun `여러 스레드가 순차적으로 포인트를 사용해야 한다`() {
        val userId = 1L
        val initialPoint = UserPoint(userId, 500L, System.currentTimeMillis())
        val amountToUse = 50L
        val transactionType = TransactionType.USE

        userPointTable.insertOrUpdate(userId, initialPoint.point)

        val pointService = PointService(userPointTable, pointHistoryTable)

        val numThreads = 5
        val latch = CountDownLatch(numThreads)
        val executor = Executors.newFixedThreadPool(numThreads)

        repeat(numThreads) {
            executor.submit {
                pointService.use(userId, amountToUse, transactionType)
                latch.countDown()
            }
        }

        latch.await()

        // 최종 포인트는 500 - 50 * 5 = 250이어야 함
        val expectedFinalPoint = 500L - (amountToUse * numThreads)
        val actualFinalPoint = pointService.get(userId).point

        assertEquals(expectedFinalPoint, actualFinalPoint)
    }

    @Test
    fun `여러 스레드가 충전과 사용을 동시에 할 때 포인트 무결성이 보장되어야 한다`() {
        val userId = 1L
        val initialPoint = UserPoint(userId, 100L, System.currentTimeMillis())
        val transactionTypeCharge = TransactionType.CHARGE
        val transactionTypeUse = TransactionType.USE

        userPointTable.insertOrUpdate(userId, initialPoint.point)

        val pointService = PointService(userPointTable, pointHistoryTable)

        val numThreads = 10
        val latch = CountDownLatch(numThreads)
        val executor = Executors.newFixedThreadPool(numThreads)

        val chargeAmount = 50L
        val useAmount = 30L

        repeat(numThreads) { i ->
            executor.submit {
                if (i % 2 == 0) {
                    pointService.charge(userId, chargeAmount, transactionTypeCharge)
                } else {
                    pointService.use(userId, useAmount, transactionTypeUse)
                }
                latch.countDown()
            }
        }

        latch.await()

        // 최종 포인트는 100 + 250 - 150 = 200이어야 함
        val expectedFinalPoint = 100L + (chargeAmount * (numThreads / 2)) - (useAmount * (numThreads / 2))
        val actualFinalPoint = pointService.get(userId).point

        assertEquals(expectedFinalPoint, actualFinalPoint)
    }
}