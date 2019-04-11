package io.spring.billrun;

import java.util.List;

import io.spring.billrun.configuration.Usage;
import javax.sql.DataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
public class BillRunApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	@Before
	public void setup()  {
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
		initializeDatabase();
	}

	@Test
	public void testJobResults() throws Exception{
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		testResult();
	}


	private void testResult() {
		List<BillStatement> billStatements = this.jdbcTemplate.query("select ID, " +
						"first_name, last_name, minutes, data_usage, bill_amount FROM bill_statements",
				(rs, rowNum) -> new BillStatement(rs.getLong("id"),
						rs.getString("FIRST_NAME"), rs.getString("LAST_NAME"),
						rs.getLong("MINUTES"), rs.getLong("DATA_USAGE"),
						rs.getDouble("bill_amount")));
		assertThat(billStatements.size()).isEqualTo(1);

		BillStatement billStatement = billStatements.get(0);
		assertThat(billStatement.getBillAmount()).isEqualTo(2.5);
		assertThat(billStatement.getFirstName()).isEqualTo("jane");
		assertThat(billStatement.getLastName()).isEqualTo("doe");
		assertThat(billStatement.getId()).isEqualTo(1);
		assertThat(billStatement.getMinutes()).isEqualTo(200);
		assertThat(billStatement.getDataUsage()).isEqualTo(500);

	}

	public void initializeDatabase() {
		this.jdbcTemplate.execute("INSERT INTO BILL_USAGE (id, first_name, " +
				"last_name, minutes, data_usage) values (1, 'jane', 'doe', 200, 500)");
	}



	public static class BillStatement extends Usage {

		public BillStatement(Long id, String firstName, String lastName, Long minutes, Long dataUsage, double billAmount) {
			super(id, firstName, lastName, minutes, dataUsage);
			this.billAmount = billAmount;
		}

		private double billAmount;

		public double getBillAmount() {
			return billAmount;
		}

		public void setBillAmount(double billAmount) {
			this.billAmount = billAmount;
		}
	}

}
