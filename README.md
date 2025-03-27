## 회원 포인트 프로젝트

***

### 요구사항

- 포인트 조회
- 포인트 충전/사용 내역 조회
- 포인트 충전
    - 행동분석
        1. 회원Id(userId), 충전포인트(amount)를 넘겨 받는다.
        2. 충전포인트가 0보다 작은지 검증한다.
        3. 현재 포인트 + 충전 포인트가 10,000 이상인지 검증한다.
        4. 충전금액을 저장한다.
        5. PointHistory를 저장한다.
    - TEST CASE
        1. 충전포인트가 0보다 작으면 예외가 발생한다.
        2. 현재 포인트와 충전 포인트가 10,000 이상이면 예외가 발생한다.
- 포인트 사용
    - 행동분석
        1. 회원Id(userId), 충전포인트(amount)를 넘겨 받는다.
        2. 사용포인트가 0보다 작은지 검증한다.
        3. 사용포인트가 보유포인트보다 큰지 검증한다.
        4. 보유포인트에서 사용포인트를 감소시킨다.
        5. 사용 PointHistory를 저장한다.
    - TEST CASE
        1. 사용포인트가 0보다 작으면 예외가 발생한다.
        2. 사용포인트가 보유포인트보다 크면 예외가 발생한다.

### 동시성 제어 방식

#### 서비스 로직에서 발생할 수 있는 동시성 이슈

chargePoint 메서드는 사용자의 포인트를 충전하는 기능을 수행하는데, 동시에 여러 요청이 들어오면 데이터 정합성이 깨질 가능성이 있다.
예를 들어, 두 개의 요청이 동시에 같은 userId에 대해 chargePoint 메서드를 실행하면,

- 첫 번째 요청이 userPointTable.selectById(userId)로 데이터를 조회
- 두 번째 요청도 거의 동시에 같은 데이터를 조회
- 각 요청이 증가된 포인트를 계산하여 업데이트
- 서로 다른 증가된 포인트 값이 덮어쓰여져 최종적으로 반영되는 데이터가 예상과 다를 수 있음

즉, 여러 요청이 동시에 실행되면 포인트 충전 로직에서 race condition(경쟁 상태)이 발생할 수 있음.
이를 방지하기 위해 동기화된 접근 방식이 필요함.

#### synchronized 대신 ConcurrentHashMap과 ReentrantLock을 사용했는가?

동시성을 제어하기위해 `synchronized`대신 `ConcurrentHashMap`과 `ReentrantLock`을 사용했습니다.

- synchronized를 사용하지 않은 이유
    - synchronized를 사용하면 메서드나 블록 전체를 하나의 모니터 락으로 보호하므로, 해당 메서드가 실행되는 동안 다른 스레드는 해당 블록에 접근할 수 없다.
    - 이는 JVM 수준에서 관리되는 락으로, 락을 해제할 때까지 다른 스레드들은 대기해야 함 → 성능 저하 가능성
    - synchronized는 세밀한 락 관리가 어렵고, 재진입 가능하지만 공정성을 보장하지 않음


- ConcurrentHashMap과 ReentrantLock을 이용한 이유
    - ConcurrentHashMap<Long, ReentrantLock>을 사용하면, userId별로 개별적인 락을 관리할 수 있음 → 전역 락이 아닌 사용자별 락을 적용 가능하다
    - ReentrantLock을 사용하면 더 세밀한 락 관리가 가능하고, 공정성 옵션을 줄 수 있어 특정 스레드가 계속 대기하는 문제를 방지할 수 있다.
    - synchronized보다 try-finally 블록을 이용해 명시적으로 관리 가능하여, 예외 발생 시에도 반드시 락을 해제할 수 있음

#### ConcurrentHashMap + ReentrantLock을 이용한 동작 방식

현재 LockManager 클래스에서는 사용자별로 개별적인 락을 관리하기 위해 ConcurrentHashMap<Long, ReentrantLock>을 사용하고 있습니다.
즉, 각 사용자 ID마다 하나의 ReentrantLock을 생성하고, 그 락을 사용하여 동시성을 제어하는 방식입니다.

(1) 락 획득 과정

1. chargePoint(userId, amount) 호출 시, LockManager에서 userId에 대한 ReentrantLock을 가져옴.
2. lock.lock();을 호출하여 해당 사용자 ID에 대한 락을 점유.
3. 만약 다른 스레드가 이미 동일한 userId에 대해 lock.lock()을 호출한 상태라면, 현재 스레드는 대기 상태에 들어감.
4. 락을 획득한 후, userPointTable.selectById(userId)를 실행하여 현재 사용자 포인트를 조회.
5. 포인트를 충전할 수 있는지 검증 후(chargeValidate(amount)), increasePoint(amount)를 통해 포인트 증가.
6. 업데이트된 데이터를 다시 userPointTable.insertOrUpdate()로 저장.
7. pointHistoryTable.insert()를 실행하여 트랜잭션 기록을 남김.

(2) 락 해제 과정

1. chargePoint 메서드 실행이 끝나면, finally 블록에서 lock.unlock();을 실행하여 락을 해제.
2. 락이 해제되면 해당 userId의 락을 기다리고 있던 다른 스레드가 깨어나서 실행 가능.
3. 대기 중인 다른 요청이 lock.lock();을 통해 락을 획득한 후 동일한 프로세스를 진행.

