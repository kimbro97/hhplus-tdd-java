package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    @Test
    void 충전포인트가_0_또는_음수이면_예외가_발생한다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 0보다 커야합니다.");

        assertThatThrownBy(() -> userPoint.charge(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 0보다 커야합니다.");
    }

    @Test
    void 충전포인트와_현재포인트의_합이_10000_이상이면_예외가_발생한다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(7001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트는 10000원을 초과할 수 없습니다.");
    }

    @Test
    void 사용포인트가_0_또는_음수이면_예외가_발생한다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야합니다.");

        assertThatThrownBy(() -> userPoint.use(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 포인트는 0보다 커야합니다.");
    }

    @Test
    void 사용포인트가_보유포인트를_초과하면_예외가_발생한다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(3001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보유 포인트가 부족합니다.");
    }

    @Test
    void 포인트를_증가시킬_수_있다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());
        long amount = userPoint.charge(1000);
        assertThat(amount).isEqualTo(4000);
    }

    @Test
    void 포인트를_감소시킬_수_있다() {
        UserPoint userPoint = new UserPoint(1, 3000, System.currentTimeMillis());
        long amount = userPoint.use(1000);
        assertThat(amount).isEqualTo(2000);
    }
}
