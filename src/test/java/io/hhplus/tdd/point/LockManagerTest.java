package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

class LockManagerTest {
    @Test
    void 같은_userId에_대해_같은_Lock_객체를_반환한다() {
        // arrange
        LockManager lockManager = new LockManager();
        Long userId = 1L;

        // act
        ReentrantLock lock1 = lockManager.getLock(userId);
        ReentrantLock lock2 = lockManager.getLock(userId);

        // assert
        assertThat(lock1).isSameAs(lock2);
    }

    @Test
    void 다른_userId에_대해_다른_Lock_객체를_반환한다() {
        // arrange
        LockManager lockManager = new LockManager();
        Long userId1 = 1L;
        Long userId2 = 2L;

        // act
        ReentrantLock lock1 = lockManager.getLock(userId1);
        ReentrantLock lock2 = lockManager.getLock(userId2);

        // assert
        assertThat(lock1).isNotSameAs(lock2);
    }

    @Test
    void 생성된_Lock이_공정모드로_설정되는지_확인한다() {
        // arrange
        LockManager lockManager = new LockManager();
        Long userId = 1L;

        // act
        ReentrantLock lock = lockManager.getLock(userId);

        // assert
        assertThat(lock.isFair()).isTrue();
    }
}
