package io.hhplus.tdd.database;

import static io.hhplus.tdd.point.TransactionType.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.point.PointHistory;

class PointHistoryTableTest {

	PointHistoryTable pointHistoryTable = new PointHistoryTable();

	@Test
	@DisplayName("userId, amount, transactionType, updateMillis을 받아서 저장할 수 있다.")
	void insert_test() {
		pointHistoryTable.insert(1, 3000, CHARGE, System.currentTimeMillis());

		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1);

		assertThat(pointHistories).hasSize(1);
	}

	@Test
	@DisplayName("userId 받아서 충전/사용 내역을 조회할 수 있다.")
	void select_test() {
		pointHistoryTable.insert(1, 3000, CHARGE, System.currentTimeMillis());
		pointHistoryTable.insert(2, 3000, CHARGE, System.currentTimeMillis());
		pointHistoryTable.insert(1, 3000, CHARGE, System.currentTimeMillis());

		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1);

		assertThat(pointHistories).hasSize(2);
	}
}
