package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.request.AmountRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PointControllerE2ETest {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserPointTable userPointTable;

	@Autowired
	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
	}

	@Test
	@DisplayName("userPoint가 5000원으로 설정되어있고, GET요청을 보내면, 응답 userPoint는 5000원이다.")
	void get_user_point() {
		// arrange
		long userId = 1L;
		long amount = 5000;
		userPointTable.insertOrUpdate(userId, amount);

		// act
		ResponseEntity<UserPoint> response = testRestTemplate.getForEntity(
			"/point/" + userId, UserPoint.class
		);

		// assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(userId);
		assertThat(response.getBody().point()).isEqualTo(amount);
	}

	@Test
	@DisplayName("pointHistory가 설정되어있고, GET요청을 보내면, 응답 리스트의 사이즈는 1이다")
	void test() {
		// arrange
		long userId = 1L;
		long amount = 5000;
		pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

		// act
		ResponseEntity<PointHistory[]> response = testRestTemplate.getForEntity(
			"/point/" + userId + "/histories", PointHistory[].class
		);

		// assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().length).isEqualTo(1);
		assertThat(response.getBody()[0].userId()).isEqualTo(userId);
		assertThat(response.getBody()[0].type()).isEqualTo(TransactionType.CHARGE);
	}

	@Test
	@DisplayName("UserPoint 5000원이 있고, 2000원 충전요청을 보내면, 회원 포인트는 7000원이다")
	void testChargePoint() {

		// arrange
		long userId = 1L;
		long amount = 5000;
		userPointTable.insertOrUpdate(userId, amount);

		// act
		AmountRequest request = new AmountRequest(2000);
		ResponseEntity<UserPoint> response = testRestTemplate.exchange(
			"/point/" + userId + "/charge",
			HttpMethod.PATCH,
			new HttpEntity<>(request),
			UserPoint.class
		);

		// assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(userId);
		assertThat(response.getBody().point()).isEqualTo(7000);
	}

	@Test
	@DisplayName("UserPoint 5000원이 있고, 2000원 사용요청을 보내면, 회원 포인트는 3000원이다")
	void testUsePoint() {

		// arrange
		long userId = 1L;
		long amount = 5000;
		userPointTable.insertOrUpdate(userId, amount);

		// act
		AmountRequest request = new AmountRequest(2000);
		ResponseEntity<UserPoint> response = testRestTemplate.exchange(
			"/point/" + userId + "/use",
			HttpMethod.PATCH,
			new HttpEntity<>(request),
			UserPoint.class
		);

		// assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(userId);
		assertThat(response.getBody().point()).isEqualTo(3000);
	}
}
