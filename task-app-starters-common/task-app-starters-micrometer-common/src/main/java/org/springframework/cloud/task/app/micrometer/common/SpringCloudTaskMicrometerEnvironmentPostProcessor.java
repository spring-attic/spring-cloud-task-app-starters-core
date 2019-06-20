/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.cloud.task.app.micrometer.common;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Disables all Micrometer Repositories added as App Starters dependencies by default.
 * That means disabling Datadog, Influx and Prometheus.
 *
 * @author Christian Tzolov
 */
public class SpringCloudTaskMicrometerEnvironmentPostProcessor implements EnvironmentPostProcessor {

	protected static final String PROPERTY_SOURCE_KEY_NAME = SpringCloudTaskMicrometerEnvironmentPostProcessor.class.getName();

	private final static String METRICS_PROPERTY_NAME_TEMPLATE = "management.metrics.export.%s.enabled";

	private final static String[] METRICS_REPOSITORY_NAMES = new String[] { "datadog", "influx", "prometheus" };

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Properties properties = new Properties();

		for (String metricsRepositoryName : METRICS_REPOSITORY_NAMES) {
			String metricsEnabledPropertyKey = String.format(METRICS_PROPERTY_NAME_TEMPLATE, metricsRepositoryName);

			// Back off if the property is already set externally
			if (!environment.containsProperty(metricsEnabledPropertyKey)) {
				properties.setProperty(metricsEnabledPropertyKey, "false");
			}
		}

		// Ensure  idempotency as this post-processor is called multiple times (once for each application context)
		// but the properties should be set only once.
		if (!properties.isEmpty()) {
			environment.getPropertySources().addLast(new PropertiesPropertySource(PROPERTY_SOURCE_KEY_NAME, properties));
		}
	}
}
