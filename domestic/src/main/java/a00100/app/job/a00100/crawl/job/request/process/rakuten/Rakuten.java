package a00100.app.job.a00100.crawl.job.request.process.rakuten;

import a00100.app.job.a00100.crawl.job.request.process.rakuten.login.Login;
import common.webBrowser.WebClient;

public class Rakuten {
	static final ThreadLocal<Rakuten> m_instances = new ThreadLocal<Rakuten>() {
		@Override
		protected Rakuten initialValue() {
			return new Rakuten();
		}
	};

	Rakuten() {
	}

	public static Rakuten getInstance() {
		return m_instances.get();
	}

	public void execute() throws Exception {
		try {
			for (WebClient client = Login.getInstance(); client != null;) {
				client = client.execute();
			}
		} finally {
			m_instances.remove();
		}
	}
}
