package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

public class PointServiceIntegrationTest {

	private PointService pointService;

	private UserPointTable userPointTable;

	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		pointService = new PointService(userPointTable, pointHistoryTable);
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

}
