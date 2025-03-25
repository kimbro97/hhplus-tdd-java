package io.hhplus.tdd.database;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.point.UserPoint;

class UserPointTableTest {

	UserPointTable userPointTable = new UserPointTable();

	@Test
	@DisplayName("id, amount 값을 받아 포인트를 증가시킬 수 있다")
	void increase_test() {
		UserPoint userPoint = userPointTable.insertOrUpdate(1, 3000);

		assertThat(userPoint.point()).isEqualTo(3000);
	}

	@Test
	@DisplayName("id, amount 값을 받아 포인트를 감소시킬 수 있다")
	void decrease_test() {
		UserPoint userPoint = userPointTable.insertOrUpdate(1, 3000);

		userPoint = userPointTable.insertOrUpdate(1, userPoint.decreasePoint(2000));

		assertThat(userPoint.point()).isEqualTo(1000);
	}

}
