package shadows.apotheosis.ench.altar;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableSet;

import shadows.apotheosis.ench.altar.IntCollector.Counter;

public class IntCollector implements Collector<Integer, Counter, Integer> {

	public static final IntCollector INSTANCE = new IntCollector();

	@Override
	public Supplier<Counter> supplier() {
		return Counter::new;
	}

	@Override
	public BiConsumer<Counter, Integer> accumulator() {
		return (c, i) -> c.count += i;
	}

	@Override
	public BinaryOperator<Counter> combiner() {
		return Counter::new;
	}

	@Override
	public Function<Counter, Integer> finisher() {
		return c -> c.count;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return ImmutableSet.of(Characteristics.UNORDERED);
	}

	static class Counter {
		int count = 0;

		Counter() {
		}

		Counter(Counter a, Counter b) {
			this.count += a.count;
			this.count += b.count;
		}
	}
}