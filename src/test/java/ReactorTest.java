import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static java.lang.System.out;

public class ReactorTest {
	private Flux<Integer> generateFluxFrom1To6() {
		return Flux.just(1, 2, 3, 4, 5, 6);
	}

	private Mono<Integer> generateMonoWithError() {
		return Mono.error(new Exception("some error"));
	}

	@Test
	public void testViaStepVerifier() {
		StepVerifier.create(generateFluxFrom1To6()).expectNext(1, 2, 3, 4, 5, 6).expectComplete().verify();
		StepVerifier.create(generateMonoWithError()).expectErrorMessage("some error").verify();
	}

	@Test
	public void testViaStepVerifier2() {
		StepVerifier.create(Flux.just("flux", "m        ono").flatMap(s -> Flux.fromArray(s.split("\\s*"))   // 1
		                                                                       .delayElements(Duration.ofMillis(100))) // 2
		                        .doOnNext(out::print)) // 3
		            .expectNextCount(9) // 4
		            .verifyComplete();
	}

	private Flux<String> getZipDescFlux() {
		String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
		return Flux.fromArray(desc.split("\\s+"));  // 1
	}

	@Test
	public void testSimpleOperators() throws InterruptedException {
		CountDownLatch countDownLatch = new CountDownLatch(1);  // 2
		Flux.zip(getZipDescFlux(), Flux.interval(Duration.ofMillis(10)))  // 3
		    .subscribe(t -> out.println(t.getT1()), null, countDownLatch::countDown);    // 4
		countDownLatch.await(10, TimeUnit.SECONDS);     // 5
	}

	@Test
	public void testGenerate() {
		Flux.generate(() -> new AtomicInteger(0), (counter, sink) -> {
			if (counter.get() == 10) {
				sink.complete();
			}
			sink.next(System.currentTimeMillis());
			counter.incrementAndGet();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return counter;
		}).subscribe(out::println);
	}

	@Test
	public void testErrorResume() {
		Flux.range(1, 6).map(i -> 10 / (i - 3)).onErrorContinue((e, i) -> out.println(i)) // 提供新的数据流
		    .map(i -> i * i).subscribe(System.out::println, System.err::println);
	}

	@Test
	public void testDoFinally() {
		LongAdder statsCancel = new LongAdder();
		 Flux.just("foo", "bar").doFinally(type -> {
			if (type == SignalType.CANCEL)  // 2
				statsCancel.increment();  // 3
		}).take(1).subscribe(out::println);
		out.println(statsCancel.intValue());
	}
	@Test
	public void testBackpressure() {
		Flux.range(1, 6)    // 1
		    .doOnRequest(n -> System.out.println("Request " + n + " values..."))    // 2
		    .subscribe(new BaseSubscriber<Integer>() {  // 3
			    @Override
			    protected void hookOnSubscribe(Subscription subscription) { // 4
				    System.out.println("Subscribed and make a request...");
				    request(1); // 5
			    }

			    @Override
			    protected void hookOnNext(Integer value) {  // 6
				    try {
					    TimeUnit.SECONDS.sleep(1);  // 7
				    } catch (InterruptedException e) {
					    e.printStackTrace();
				    }
				    System.out.println("Get value [" + value + "]");    // 8
				    request(1); // 9
			    }
		    });
	}
}
