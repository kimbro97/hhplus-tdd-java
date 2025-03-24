package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

	@Mock
	private UserPointTable userPointTable;

	@InjectMocks
	private PointService pointService;

	@Test
	void id_값으로_회원의_포인트를_조회할_수_있다() {

		when(userPointTable.selectById(1L))
			.thenReturn(new UserPoint(1, 300, System.currentTimeMillis()));

		UserPoint userPoint = pointService.getUserPoint(1);

		assertThat(userPoint.point()).isEqualTo(300);

	}
}
