package io.spring.billrun;

import io.spring.billrun.configuration.BillingConfiguration;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
//ContextConfiguration(classes = {BillingConfiguration.class, EmbeddedDataSourceConfiguration.class})
public class BillRunApplicationTests {

//	@Autowired
//	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	DataSource dataSource;

	@Before
	public void setup()  {
		initializeDatabase();
	}

//	@Test
	public void contextLoads() throws Exception{
		JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();


		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus());
	}


	public void initializeDatabase() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
		jdbcTemplate.execute("CREATE TABLE bill_usage ( id int, first_name varchar(50), last_name varchar(50), minutes int, data_usage int)");
		jdbcTemplate.execute("CREATE TABLE bill_statements ( id int, first_name varchar(50), last_name varchar(50), minutes int, data_usage int, bill_amount decimal(10,2))");
		int result = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM bill_usage", Integer.class);
		System.out.println(result);
	}
}
