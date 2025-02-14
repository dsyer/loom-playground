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
package loomdemo.simplewebapp;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller measuring concurrency.
 *
 * @author Mark Paluch
 */
@RestController
public class MyController {

	private static AtomicLong concurrency = new AtomicLong();

	@GetMapping("/")
	public String index() {
		return Thread.currentThread().toString();
	}

	@GetMapping("/sleep/{amount}")
	public String sleep(@PathVariable long amount) throws InterruptedException {

		String concurrency = "" + MyController.concurrency.incrementAndGet();
		try {
			Thread.sleep(amount);
			return concurrency;
		}
		finally {
			MyController.concurrency.decrementAndGet();
		}
	}

}
