package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointServiceIntegrationTest {
    
    private PointService pointService;

    private UserPointTable userPointTable;

    private LockManager lockManager;

    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        lockManager = new LockManager();
        pointService = new PointService(userPointTable, pointHistoryTable, lockManager);
    }

    @Test
    @DisplayName("userPoint가 설정되어 있고, 포인트 충전시 충전포인트가 0또는 음수면, 예외가 발생한다")
    void charge_point_0_or_음수_exception() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);
        // act + assert
        assertThatThrownBy(() -> pointService.chargePoint(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 0보다 커야합니다.");

        assertThatThrownBy(() -> pointService.chargePoint(1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 0보다 커야합니다.");
    }

    @Test
    @DisplayName("userPoint가 설정되어 있고, 포인트 충전시 보유포인트와 충전포인트의 합이 10000 보다 크다면, 예외가 발생한다")
    void charge_point_max_point_exception() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);
        // act + assert
        assertThatThrownBy(() -> pointService.chargePoint(1, 5001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트는 10000원을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("userPoint가 설정되어 있고, 포인트 사용시 사용포인트가 0또는 음수면, 예외가 발생한다")
    void use_point_0_or_음수_exception() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);
        // act + assert
        assertThatThrownBy(() -> pointService.usePoint(userId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야합니다.");

        assertThatThrownBy(() -> pointService.usePoint(1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야합니다.");
    }

    @Test
    @DisplayName("userPoint가 설정되어 있고, 포인트 사용시 보유포인트보다 사용포인트가 크면, 예외가 발생한다")
    void use_point_max_point_exception() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);
        // act + assert
        assertThatThrownBy(() -> pointService.usePoint(userId, 5001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유 포인트가 부족합니다.");
    }

    @Test
    @DisplayName("userPoint가 5000원으로 설정되어 있고, 포인트를 4999원 충전하면, 회원의 포인트는 9999원이다")
    void charge_point_success() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);

        // act + assert
        pointService.chargePoint(userId, 4999);

        // assert
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(9999);
    }

    @Test
    @DisplayName("userPoint가 5000원으로 설정되어 있고, 포인트를 4999원 충전하면, 충전내역이 생긴다")
    void charge_point_point_history_success() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);

        // act + assert
        pointService.chargePoint(userId, 4999);

        // assert
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(pointHistories.size()).isEqualTo(1);
        assertThat(pointHistories.get(0).amount()).isEqualTo(4999);
        assertThat(pointHistories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistories.get(0).userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("userPoint가 5000원으로 설정되어 있고, 포인트를 4999원 사용하면, 회원의 포인트는 1원이다")
    void use_point_success() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);

        // act + assert
        pointService.usePoint(userId, 4999);

        // assert
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(1);
    }

    @Test
    @DisplayName("userPoint가 5000원으로 설정되어 있고, 포인트를 4999원 사용하면, 사용내역이 생긴다")
    void use_point_point_history_success() {
        // arrange
        long userId = 1L;
        long amount = 5000;

        userPointTable.insertOrUpdate(userId, amount);

        // act + assert
        pointService.usePoint(userId, 4999);

        // assert
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(pointHistories.size()).isEqualTo(1);
        assertThat(pointHistories.get(0).amount()).isEqualTo(4999);
        assertThat(pointHistories.get(0).type()).isEqualTo(TransactionType.USE);
        assertThat(pointHistories.get(0).userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("한명의 유저가 동시에 10번 500원씩 충전한다면 회원의 보유포인트는 총 5000원이다")
    void chargePoints_ConcurrentRequests_ShouldUpdateCorrectly() throws InterruptedException {

        int threadCount = 10;
        long chargePoint = 500;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(1, chargePoint);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint userPoint = userPointTable.selectById(1L);
        assertThat(userPoint.point()).isEqualTo(5000);
    }

    @Test
    @DisplayName("세명의 유저가 각각 세번씩 동시에 포인트를 충전해도 정상적으로 처리되어야 한다")
    void chargePoints_ThreeUsers_ThreeTimesEach_ShouldBeProcessedCorrectly() throws InterruptedException {

        int userCount = 3;
        int requestCount = 3;
        int threadCount = userCount * requestCount;
        long chargePoint = 500;

        userPointTable.insertOrUpdate(1, 1500);
        userPointTable.insertOrUpdate(2, 2000);
        userPointTable.insertOrUpdate(3, 1800);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (long i = 1; i <= userCount; i++) {
            long userId = i;
            for (int j = 0; j < requestCount; j++) {
                executorService.submit(() -> {
                    try {
                        pointService.chargePoint(userId, chargePoint);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        executorService.shutdown();

        UserPoint userPoint1 = userPointTable.selectById(1L);
        UserPoint userPoint2 = userPointTable.selectById(2L);
        UserPoint userPoint3 = userPointTable.selectById(3L);

        assertThat(userPoint1.point()).isEqualTo(3000);
        assertThat(userPoint2.point()).isEqualTo(3500);
        assertThat(userPoint3.point()).isEqualTo(3300);
    }

    @Test
    @DisplayName("한 명의 유저가 포인트를 충전하고 사용하는 요청을 동시에 보내면 정상적으로 처리되어야 한다")
    void chargeAndUsePoints_SingleUser_ConcurrentRequests_ShouldBeProcessedCorrectly() throws InterruptedException {
        long userId = 1L;
        long chargePoint = 1000L;
        long usePoint = 500L;

        userPointTable.insertOrUpdate(userId, 1000L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 포인트 충전 작업
        executorService.submit(() -> {
            try {
                pointService.chargePoint(userId, chargePoint);
            } finally {
                latch.countDown();
            }
        });

        // 포인트 사용 작업
        executorService.submit(() -> {
            try {
                pointService.usePoint(userId, usePoint);
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(1500L);
    }
}
