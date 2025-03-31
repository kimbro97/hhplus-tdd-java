package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    private static final long MAX_USER_POINT = 10000;
    private static final long MIN_POINT_AMOUNT = 0;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long charge(long amount) {
        if (amount <= MIN_POINT_AMOUNT) {
            throw new IllegalArgumentException("충전 포인트는 0보다 커야합니다.");
        }

        if (this.point + amount > MAX_USER_POINT) {
            throw new IllegalArgumentException("포인트는 10000원을 초과할 수 없습니다.");
        }

        return this.point + amount;
    }

    public long use(long amount) {

        if (amount <= MIN_POINT_AMOUNT) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야합니다.");
        }

        if (this.point < amount) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        return this.point - amount;
    }
}
