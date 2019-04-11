/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.billrun.configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.io.Resource;
import org.springframework.instrument.classloading.LoadTimeWeaver;


@Configuration
@EnableBatchProcessing
public class BillingConfiguration {
	private static final Log logger = LogFactory.getLog(BillingConfiguration.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value("${usage.file.name:file:/Users/glennrenfro/tmp/usageinfo.txt}")
	private Resource usageResource;

	@Bean
	public Job job1(ItemReader<Usage> reader, ItemProcessor<Usage,Bill> itemProcessor, ItemWriter<Bill> writer) {
		Step step = stepBuilderFactory.get("BillProcessing")
				.<Usage, Bill>chunk(1)
				.reader(reader)
				.processor(itemProcessor)
				.writer(writer)
				.build();

		return jobBuilderFactory.get("BillJob")
				.start(step)
				.build();
	}

	@Bean
	public JdbcCursorItemReader<Usage> jdbcItemReader(DataSource dataSource) {
		return  new JdbcCursorItemReaderBuilder<Usage>()
				.dataSource(dataSource)
				.name("usageReader")
				.sql("SELECT * from BILL_USAGE")
				.saveState(false)
				.rowMapper((rs, rowNum) -> {
					return new Usage(rs.getLong("ID"), rs.getString("FIRST_NAME"),
							rs.getString("LAST_NAME"), rs.getLong("MINUTES"), rs.getLong("DATA_USAGE"));
				})
				.build();
	}

	@Bean
	public ItemWriter<Bill> jdbcBillWriter(DataSource dataSource) {
		JdbcBatchItemWriter<Bill> writer = new JdbcBatchItemWriterBuilder<Bill>()
						.beanMapped()
				.dataSource(dataSource)
				.sql("INSERT INTO BILL_STATEMENTS (id, first_name, last_name, minutes, data_usage,bill_amount) VALUES (:id, :firstName, :lastName, :minutes, :dataUsage, :billAmount)")
				.build();
		return writer;
	}

	@Bean
	ItemProcessor<Usage, Bill> billProcessor() {
		return new BillProcessor();
	}

	/**
	 * Work-around for https://github.com/spring-projects/spring-boot/issues/13042
	 */
	@Configuration
	protected static class DataSourceInitializerInvokerConfiguration implements LoadTimeWeaverAware {

		@Autowired
		private ListableBeanFactory beanFactory;

		@PostConstruct
		public void init() {
			String cls = "org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerInvoker";
			if (beanFactory.containsBean(cls)) {
				beanFactory.getBean(cls);
			}
		}

		@Override
		public void setLoadTimeWeaver(LoadTimeWeaver ltw) {
		}
	}
}
