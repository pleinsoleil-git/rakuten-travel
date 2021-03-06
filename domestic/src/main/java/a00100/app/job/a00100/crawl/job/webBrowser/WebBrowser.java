package a00100.app.job.a00100.crawl.job.webBrowser;

import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebBrowser extends common.webBrowser.WebBrowser implements AutoCloseable {
	static final ThreadLocal<WebBrowser> m_instances = new ThreadLocal<WebBrowser>() {
		@Override
		protected WebBrowser initialValue() {
			return new WebBrowser();
		}
	};
	static final ConcurrentHashMap<Long, _Current> m_currents = new ConcurrentHashMap<Long, _Current>();

	WebBrowser() {
	}

	public static WebBrowser getInstance() {
		return m_instances.get();
	}

	public static _Current getCurrent() {
		val k = Thread.currentThread().getId();
		_Current v = m_currents.get(k);

		if (v == null) {
			m_currents.put(k, v = new _Current());
		}

		return v;
	}

	@Override
	public void close() throws Exception {
		try {
			for (val v : m_currents.values()) {
				v.close();
			}
		} finally {
			m_currents.clear();
			m_instances.remove();
		}
	}

	public static class _Current implements AutoCloseable {
		WebDriver m_webDriver;

		public WebDriver getWebDriver() throws Exception {
			if (m_webDriver == null) {
				m_webDriver = new ChromeDriver(new ChromeOptions() {
					{
						addArguments("--incognito"); // シークレットモード
					}
				});
			}

			return m_webDriver;
		}

		@Override
		public void close() throws Exception {
			if (m_webDriver != null) {
				log.debug("Browser close!!");
				//m_webDriver.quit();
			}
		}
	}
}
