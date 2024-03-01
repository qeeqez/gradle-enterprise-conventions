/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.ge.conventions.gradle;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.caching.BuildCacheServiceFactory;
import org.gradle.caching.configuration.BuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.caching.local.DirectoryBuildCache;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildCacheConventions}.
 *
 * @author Andy Wilkinson
 */
class BuildCacheConventionsTests {

	private final TestBuildCacheConfiguration buildCache = new TestBuildCacheConfiguration();

	@Test
	void localCacheIsEnabled() {
		new BuildCacheConventions().execute(this.buildCache);
		assertThat(this.buildCache.local.isEnabled()).isTrue();
	}

	@Test
	void remoteCacheIsEnabled() {
		new BuildCacheConventions().execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getUrl()).isEqualTo(URI.create("https://ge.spring.io/cache/"));
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void remoteCacheUrlCanBeConfigured() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_URL", "https://ge.example.com/cache/");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getUrl()).isEqualTo(URI.create("https://ge.example.com/cache/"));
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void whenCredentialsAreProvidedThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_USERNAME", "user");
		env.put("GRADLE_ENTERPRISE_CACHE_PASSWORD", "secret");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isPush()).isTrue();
		assertThat(this.buildCache.remote.getCredentials().getUsername()).isEqualTo("user");
		assertThat(this.buildCache.remote.getCredentials().getPassword()).isEqualTo("secret");
	}

	private static final class TestBuildCacheConfiguration implements BuildCacheConfiguration {

		private final DirectoryBuildCache local = new DirectoryBuildCache();

		private final HttpBuildCache remote = new HttpBuildCache();

		@Override
		public DirectoryBuildCache getLocal() {
			return this.local;
		}

		@Override
		public BuildCache getRemote() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void local(Action<? super DirectoryBuildCache> action) {
			action.execute(this.local);
		}

		@Override
		public <T extends BuildCache> void registerBuildCacheService(Class<T> cacheType,
				Class<? extends BuildCacheServiceFactory<? super T>> factory) {
			throw new UnsupportedOperationException();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends BuildCache> T remote(Class<T> cacheType) {
			return (T) this.remote;
		}

		@Override
		public void remote(Action<? super BuildCache> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BuildCache> T remote(Class<T> type, Action<? super T> action) {
			T cache = remote(type);
			action.execute(cache);
			return cache;
		}

	}

}
