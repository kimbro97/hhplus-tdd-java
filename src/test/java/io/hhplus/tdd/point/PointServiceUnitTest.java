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
class PointServiceUnitTest {

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

	private List<PointHistory> createPointHistoryList() {
		return List.of(
			new PointHistory(1, 1, 3000, TransactionType.CHARGE, System.currentTimeMillis()),
			new PointHistory(2, 1, 2000, TransactionType.USE, System.currentTimeMillis()),
			new PointHistory(3, 1, 3500, TransactionType.CHARGE, System.currentTimeMillis())
		);
	}
}
