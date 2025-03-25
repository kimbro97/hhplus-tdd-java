package io.hhplus.tdd.point;

public record UserPoint(
	long id,
	long point,
	long updateMillis
) {

	public static UserPoint empty(long id) {
		return new UserPoint(id, 0, System.currentTimeMillis());
	}

	public void chargeValidate(long amount) {

		if (amount <= 0) {
			throw new IllegalArgumentException("충전 포인트는 0보다 커야합니다.");
		}

		if (this.point + amount > 10000) {
			throw new IllegalArgumentException("포인트는 10000원을 초과할 수 없습니다.");
		}
	}

	public void useValidate(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("사용 포인트는 0보다 커야합니다.");
		}

		if (this.point < amount) {
			throw new IllegalArgumentException("보유 포인트가 부족합니다.");
		}
	}
}
