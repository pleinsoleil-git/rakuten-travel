package a00100.app.job.a00100.crawl.job.request.process;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import a00100.app.job.a00100.crawl.Connection;
import a00100.app.job.a00100.crawl.job.request.Request;
import a00100.app.job.a00100.crawl.job.request.RequestType;
import a00100.app.job.a00100.crawl.job.request.process.rakuten.Rakuten;
import common.app.job.JobStatus;
import common.jdbc.JDBCParameterList;
import common.lang.NotSupportedException;
import lombok.Data;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(prefix = "m_", chain = false)
public class Process {
	static final int MAX_THREAD_NUMS = 5;
	static final ThreadLocal<Process> m_instances = new ThreadLocal<Process>() {
		@Override
		protected Process initialValue() {
			return new Process();
		}
	};
	static final ThreadLocal<_Current> m_currents = new ThreadLocal<_Current>();
	Set<Long> m_runningIds;

	Process() {
	}

	public static Process getInstance() {
		return m_instances.get();
	}

	public static _Current getCurrent() {
		return m_currents.get();
	}

	Set<Long> getRunningIds() {
		return (m_runningIds == null ? m_runningIds = new HashSet<>() : m_runningIds);
	}

	public void execute() throws Exception {
		try {
			val executor = Executors.newFixedThreadPool(MAX_THREAD_NUMS);
			val completion = new ExecutorCompletionService<_Task>(executor);

			try {
				val ids = getRunningIds();

				do {
					for (val r : query()) {
						ids.add(r.getId());
						completion.submit(r);
					}

					if (ids.size() > 0) {
						val process = completion.take().get();
						m_currents.set(process);
						process.execute();
						ids.remove(process.getId());
					}
				} while (ids.size() > 0);

				executor.shutdown();
			} catch (Exception e) {
				executor.shutdownNow();
				throw e;
			}
		} finally {
			m_currents.remove();
			m_instances.remove();
		}
	}

	Collection<_Task> query() throws Exception {
		String sql;
		sql = "WITH s_params AS\n"
			+ "(\n"
				+ "SELECT ?::BIGINT AS request_id,\n"
					+ "?::NUMERIC AS success\n"
			+ ")\n"
			+ "SELECT j30.id,\n"
				+ "j20.job_type AS jobType,\n"
				+ "j10.request_type AS requestType,\n"
				+ "j20.user_id AS userId,\n"
				+ "j20.password,\n"
				+ "j20.check_in_date AS checkInDate,\n"
				+ "j20.check_out_date AS checkOutDate,\n"
				+ "j20.room_nums AS roomNums,\n"
				+ "j20.adult_nums AS adultNums,\n"
				+ "j20.upper_grade_nums AS upperGradeNums,\n"
				+ "j20.lower_grade_nums AS lowerGradeNums,\n"
				+ "j30.hotel_code AS hotelCode\n"
			+ "FROM s_params AS t10\n"
			+ "INNER JOIN j_crawl_request AS j10\n"
				+ "ON j10.id = t10.request_id\n"
				+ "AND j10.aborted = FALSE\n"
			+ "INNER JOIN j_crawl_job AS j20\n"
				+ "ON j20.id = j10.foreign_id\n"
				+ "AND j20.aborted = FALSE\n"
			+ "INNER JOIN j_crawl_process AS j30\n"
				+ "ON j30.foreign_id = j10.id\n"
				+ "AND j30.aborted = FALSE\n"
				+ "AND j30.deleted = FALSE\n"
			+ "WHERE NOT EXISTS\n"
			+ "(\n"
				+ "SELECT NULL\n"
				+ "FROM j_crawl_process_status AS j900\n"
				+ "WHERE j900.foreign_id = j30.id\n"
				+ "AND j900.status = t10.success\n"
			+ ")\n";

		val ids = getRunningIds();
		if (ids.isEmpty() == false) {
			sql += "AND j30.id NOT IN\n"
				+ "(\n"
					+ StringUtils.repeat("?::BIGINT", ",\n", ids.size())
				+ ")\n";
		}

		sql += "ORDER BY j30.priority NULLS LAST,\n"
				+ "j30.id\n";

		val rs = new BeanListHandler<_Task>(_Task.class);
		return Connection.App.query(sql, rs, new JDBCParameterList() {
			{
				val request = Request.getCurrent();
				add(request.getId());
				add(JobStatus.SUCCESS.original());

				for (val id : ids) {
					add(id);
				}
			}
		});
	}

	@Data
	public static class _Current {
		Long m_id;
		String m_jobType;
		String m_requestType;
		String m_userId;
		String m_password;
		Date m_checkInDate;
		Date m_checkOutDate;
		Long m_roomNums;
		Long m_adultNums;
		Long m_upperGradeNums;
		Long m_lowerGradeNums;
		String m_hotelCode;
		Status m_status;

		public Status getStatus() {
			return (m_status == null ? m_status = new Status() : m_status);
		}

		void execute() throws Exception {
			try (val status = getStatus()) {
			}
		}
	}

	public static class _Task extends _Current implements Callable<_Task> {
		@Override
		public _Task call() {
			log.info(String.format("Process[id=%d]", getId()));

			try {
				m_currents.set(this);
				run();
			} finally {
				m_currents.remove();
			}

			return this;
		}

		void run() {
			val status = getStatus();

			try {
				if (aborted() == true) {
					status.setStatus(JobStatus.ABORT);
				} else {
					switch (RequestType.valueOf(getRequestType())) {
					case RAKUTEN:
						//rakuten();
						break;
					default:
						throw new NotSupportedException();
					}

					status.setStatus(JobStatus.SUCCESS);
				}
			} catch (Exception e) {
				status.setStatus(JobStatus.FAILD);
				status.setErrorMessage(e.getMessage());
				log.error("", e);
			}
		}

		void rakuten() throws Exception {
			Rakuten.getInstance().execute();
		}

		boolean aborted() throws Exception {
			String sql;
			sql = "WITH s_params AS\n"
				+ "(\n"
					+ "SELECT ?::BIGINT AS process_id\n"
				+ ")\n"
				+ "SELECT j10.aborted\n"
				+ "FROM s_params AS t10\n"
				+ "INNER JOIN j_crawl_process AS j10\n"
					+ "ON j10.id = t10.process_id\n"
				+ "INNER JOIN j_crawl_request AS j20\n"
					+ "ON j20.id = j10.foreign_id\n"
					+ "AND j20.aborted = FALSE\n"
				+ "INNER JOIN j_crawl_job AS j30\n"
					+ "ON j30.id = j20.foreign_id\n"
					+ "AND j30.aborted = FALSE\n";

			val rs = new ScalarHandler<Boolean>();
			return BooleanUtils.isTrue(Connection.App.query(sql, rs, new JDBCParameterList() {
				{
					add(getId());
				}
			}));
		}
	}
}
