package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class LockManager {

    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public <T> T executeWithLock(Long userId, Supplier<T> task) {

        ReentrantLock lock = lockMap.computeIfAbsent(userId, k -> new ReentrantLock(true));

        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }

    }

    // 단위테스트용 get 메서드
    public ReentrantLock getLock(Long userId) {
        return lockMap.get(userId);
    }
}
