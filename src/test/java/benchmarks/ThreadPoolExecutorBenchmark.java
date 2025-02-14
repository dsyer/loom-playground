/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package benchmarks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark running {@link Runnable}s using {@link ThreadPoolExecutor}.
 *
 * @author Mark Paluch
 */
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 2)
@Threads(8)
@Fork(value = 1,
		jvmArgs = { "--enable-preview", "-server", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m", "-Xmx1024m",
				"-XX:MaxDirectMemorySize=1024m", "-noverify" })
@State(Scope.Benchmark)
@Testable
public class ThreadPoolExecutorBenchmark {

	ExecutorService executor;

	@Setup
	public void setUp() {
		executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
	}

	@TearDown
	public void tearDown() {
		executor.close();
	}

	@Benchmark
	@Testable
	public void runAndAwait(Blackhole sink) throws ExecutionException, InterruptedException {
		sink.consume(executor.submit(() -> {
		}).get());
	}

	@Benchmark
	@Testable
	public void readFiles(Blackhole sink) throws ExecutionException, InterruptedException {
		ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<>(executor);
		AtomicInteger count = new AtomicInteger();
		try (Stream<Path> files = Files.walk(Path.of("target"))) {
			files.filter(file -> !Files.isDirectory(file)).forEach(path -> {
				try {
					service.submit(() -> {
						try {
							sink.consume(Files.readAllBytes(path));
						}
						catch (IOException e) {
							// ignore
						}
					}, true);
					count.incrementAndGet();
				}
				catch (Exception e) {
					// ignore
					e.printStackTrace();
				}
			});
		}
		catch (Exception e) {
			// ignore
			e.printStackTrace();
		}
		for (int i = 0; i < count.get(); i++) {
			service.take();
		}
	}

}
