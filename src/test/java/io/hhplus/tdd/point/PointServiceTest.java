package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

	@Mock
	private UserPointTable userPointTable;

	@Mock
	private PointHistoryTable pointHistoryTable;

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
	void 충전포인트가_0이거나_0보다_작다면_예외가_발생한다() {

		when(userPointTable.selectById(1L))
			.thenReturn(new UserPoint(1, 4000, System.currentTimeMillis()));

		assertThatThrownBy(() -> pointService.chargePoint(1, -1))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("충전 포인트는 0보다 커야합니다.");

		assertThatThrownBy(() -> pointService.chargePoint(1, 0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("충전 포인트는 0보다 커야합니다.");
	}

	@Test
	void 충전포인트와_현재포인트의_합이_10000이상일때_예외가_발생한다() {
		when(userPointTable.selectById(1L))
			.thenReturn(new UserPoint(1, 4000, System.currentTimeMillis()));

		assertThatThrownBy(() -> pointService.chargePoint(1, 6001))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("포인트는 10000원을 초과할 수 없습니다.");
	}

	private List<PointHistory> createPointHistoryList() {
		return List.of(
			new PointHistory(1, 1, 3000, TransactionType.CHARGE, System.currentTimeMillis()),
			new PointHistory(2, 1, 2000, TransactionType.USE, System.currentTimeMillis()),
			new PointHistory(3, 1, 3500, TransactionType.CHARGE, System.currentTimeMillis())
		);
	}
}
