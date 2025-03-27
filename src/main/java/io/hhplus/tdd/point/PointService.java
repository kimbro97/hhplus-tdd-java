package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	public UserPoint getUserPoint(long id) {
		return userPointTable.selectById(id);
	}

	public List<PointHistory> getPointHistory(long userId) {
		return pointHistoryTable.selectAllByUserId(userId);
	}

	public UserPoint chargePoint(long userId, long amount) {

		UserPoint userPoint = userPointTable.selectById(userId);

		userPoint.chargeValidate(amount);

		userPoint = userPointTable.insertOrUpdate(userId, userPoint.increasePoint(amount));

		pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

		return userPoint;
	}

	public UserPoint usePoint(long userId, long amount) {

		UserPoint userPoint = userPointTable.selectById(userId);

		userPoint.useValidate(amount);

		userPoint = userPointTable.insertOrUpdate(userId, userPoint.decreasePoint(amount));

		pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

		return userPoint;
	}
}
