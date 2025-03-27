package io.hhplus.tdd.database;

import io.hhplus.tdd.point.PointHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.Assertions.assertThat;

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
