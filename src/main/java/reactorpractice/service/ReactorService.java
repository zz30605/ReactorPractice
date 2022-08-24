package reactorpractice.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorService {
    @Test
    public void test() {
        Flux<Integer> just = Flux.just(1,2,3,4,5);
        just.subscribe(System.out::println, System.err::println, () -> System.out.println("Completed!"));
    }
}
