package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private LockManager lockManager;

    @InjectMocks
    private PointService pointService;

    @Test
    void id_값으로_회원의_포인트를_조회할_수_있다() {

        when(userPointTable.selectById(1L))
                .thenReturn(new UserPoint(1, 300, System.currentTimeMillis()));

        UserPoint userPoint = pointService.getUserPoint(1);

        assertThat(userPoint.point()).isEqualTo(300);

    }

    @Test
    void userId_값으로_포인트_내역을_조회할_수_있다() {

        when(pointHistoryTable.selectAllByUserId(1))
                .thenReturn(createPointHistoryList());

        List<PointHistory> pointHistory = pointService.getPointHistory(1);

        assertThat(pointHistory).hasSize(3);

    }

    @Test
    void charge_point_success() {
        // arrange
        when(lockManager.executeWithLock(eq(1L), any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        when(userPointTable.selectById(1L))
                .thenReturn(new UserPoint(1, 3000, System.currentTimeMillis()));

        when(userPointTable.insertOrUpdate(1L, 7000))
                .thenReturn(new UserPoint(1, 7000, System.currentTimeMillis()));

        // act
        UserPoint userPoint = pointService.chargePoint(1L, 4000);

        // assert
        assertThat(userPoint.point()).isEqualTo(7000);
    }

    @Test
    void use_point_success() {
        // arrange
        when(lockManager.executeWithLock(eq(1L), any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        when(userPointTable.selectById(1L))
                .thenReturn(new UserPoint(1, 3000, System.currentTimeMillis()));

        when(userPointTable.insertOrUpdate(1L, 1000))
                .thenReturn(new UserPoint(1, 1000, System.currentTimeMillis()));

        // act
        UserPoint userPoint = pointService.usePoint(1L, 2000);

        // assert
        assertThat(userPoint.point()).isEqualTo(1000);
    }

    @Test
    @DisplayName("충전 금액을 0으로 받으면 insertOrUpdate 메소드는 실행되지 않는다")
    void chargePoint_withZeroAmount_doesNotCallInsertOrUpdate() {
        // arrange
        when(lockManager.executeWithLock(eq(1L), any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        when(userPointTable.selectById(1L))
                .thenReturn(new UserPoint(1, 3000, System.currentTimeMillis()));

        // act
        assertThatThrownBy(() -> {
            pointService.chargePoint(1L, 0);
        }).isInstanceOf(IllegalArgumentException.class);

        // assert
        verify(userPointTable, times(0)).insertOrUpdate(1L, 0);
        verify(pointHistoryTable, times(0)).insert(1L, 0, TransactionType.CHARGE, System.currentTimeMillis());
    }

    @Test
    @DisplayName("충전 금액과 보유 금액이 10000원 이상이면 insertOrUpdate, insert 메소드는 실행되지 않는다")
    void should_not_execute_insertOrUpdate_if_amount_exceeds_limit() {
        // arrange
        when(lockManager.executeWithLock(eq(1L), any())).thenAnswer(invocation -> {
            Supplier<UserPoint> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        when(userPointTable.selectById(1L))
                .thenReturn(new UserPoint(1, 3000, System.currentTimeMillis()));

        // act
        assertThatThrownBy(() -> {
            pointService.chargePoint(1L, 7001);
        }).isInstanceOf(IllegalArgumentException.class);

        // assert
        verify(userPointTable, times(0)).insertOrUpdate(1L, 7001);
        verify(pointHistoryTable, times(0)).insert(1L, 7001, TransactionType.CHARGE, System.currentTimeMillis());
    }

    private List<PointHistory> createPointHistoryList() {
        return List.of(
                new PointHistory(1, 1, 3000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, 1, 2000, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(3, 1, 3500, TransactionType.CHARGE, System.currentTimeMillis())
        );
    }
}
