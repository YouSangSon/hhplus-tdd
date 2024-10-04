
---

# 동시성 제어 방식

### ReentrantLock vs Synchronized
코루틴을 사용하지 않고 동시성을 제어하는 방법으로는 `ReentrantLock`과 `synchronized`가 있습니다.   
두 방법 모두 공유 자원에 대한 동기화를 제공하지만, `ReentrantLock`을 선택한 이유는 다음과 같습니다.

### ReentrantLock와 Synchronized의 차이
1. **락의 획득과 해제 제어**
   - **ReentrantLock**: `lock()`과 `unlock()`을 통해 락을 명시적으로 획득하고 해제할 수 있습니다. 이는 복잡한 동기화 로직에서 세밀한 제어가 가능하게 합니다.
   - **synchronized**: 자동으로 락을 획득하고 해제하지만, 락을 해제하는 시점을 명시적으로 제어할 수 없습니다.


2. **조건 변수 지원**
   - **ReentrantLock**: `Condition` 객체를 사용하여 복잡한 조건에서 스레드를 대기시키고 깨울 수 있습니다.
   - **synchronized**: `wait()`, `notify()`, `notifyAll()`을 사용하지만, 단일 조건만 지원합니다.


3. **타임아웃 기능**
   - **ReentrantLock**: `tryLock(long timeout, TimeUnit unit)`을 사용하여 락 획득에 대한 타임아웃을 설정할 수 있습니다.
   - **synchronized**: 타임아웃 기능을 지원하지 않아, 무한 대기가 발생할 수 있습니다.


4. **락의 공정성(fairness)**
   - **ReentrantLock**: 공정성 모드를 설정하면 대기 중인 스레드가 락을 획득하는 순서를 제어할 수 있습니다.
   - **synchronized**: 공정성을 보장하지 않으며, 락 획득 순서를 예측할 수 없습니다.

### ReentrantLock을 선택한 이유
1. **락의 명시적 관리** : `ReentrantLock`은 `lock()`과 `unlock()`을 통해 락을 명시적으로 관리할 수 있으므로, 각 사용자의 포인트 데이터를 세밀하게 제어할 수 있습니다. 이로 인해 동시성 이슈를 확실히 방지할 수 있습니다.


2. **타임아웃 기능 지원**: `ReentrantLock`은 타임아웃을 설정하여 특정 시간 내에 락을 획득하지 못하면 다른 처리를 할 수 있도록 지원합니다. 이는 교착 상태(Deadlock)를 방지할 수 있습니다.


3. **공정성 보장**: 공정성(fairness) 설정을 통해 대기 중인 스레드들이 순서대로 락을 획득할 수 있어, 특정 스레드가 락을 계속해서 얻지 못하는 문제를 방지할 수 있습니다.


4. **복잡한 동기화 로직 처리 가능**: `ReentrantLock`은 `Condition` 객체를 사용하여 여러 조건을 세밀하게 처리할 수 있습니다. 이는 단순한 `synchronized`로는 해결할 수 없는 복잡한 동기화 문제를 처리하는 데 매우 유리합니다.

---

# zz