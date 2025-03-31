package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final LockManager lockManager;

    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {
        ReentrantLock lock = lockManager.getLock(userId);

        lock.lock();
        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            long point = userPoint.charge(amount);

            userPoint = userPointTable.insertOrUpdate(userId, point);

            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint usePoint(long userId, long amount) {

        ReentrantLock lock = lockManager.getLock(userId);

        lock.lock();
        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            long point = userPoint.use(amount);

            userPoint = userPointTable.insertOrUpdate(userId, point);

            pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }
    }
}
