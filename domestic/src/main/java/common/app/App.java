package common.app;

import common.jdbc.JDBCConnection;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(prefix = "m_", chain = false)
public abstract class App {
	static final ThreadLocal<App> m_instances = new ThreadLocal<App>();

	@Getter
	final Bean m_bean;

	@Getter
	final Model m_model;

	JDBCConnection m_connection;

	public App(final Bean bean, final Model model) {
		m_bean = bean;
		m_model = model;
	}

	public abstract String getDataSourceName();

	public static App getInstance() {
		return m_instances.get();
	}

	public void initInstance() {
		log.debug("Init instance");

		m_instances.set(this);
	}

	public void exitInstance() {
		log.debug("Exit instance");

		try {
			if (m_connection != null) {
				m_connection.close();
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			m_instances.remove();
		}
	}

	public JDBCConnection getConnection() {
		return (m_connection == null ? m_connection = new JDBCConnection(getDataSourceName()) : m_connection);
	}
}
