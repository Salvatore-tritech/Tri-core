package com.tritech.tricore;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		CamundaBpmAutoConfiguration.class
})
@ActiveProfiles("test")
class TriCoreApplicationTests {

	@Test
	void contextLoads() {
		// Empty test method
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public PlatformTransactionManager testTransactionManager() {
			return new MockTransactionManager();
		}

		@Bean
		public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
			return new TransactionTemplate(transactionManager);
		}
	}

	// Mock TransactionManager to resolve transaction-related dependencies
	static class MockTransactionManager implements PlatformTransactionManager {
		@Override
		public org.springframework.transaction.TransactionStatus getTransaction(org.springframework.transaction.TransactionDefinition definition) {
			return null;
		}

		@Override
		public void commit(org.springframework.transaction.TransactionStatus status) {
		}

		@Override
		public void rollback(org.springframework.transaction.TransactionStatus status) {
		}
	}
}