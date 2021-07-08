package a00100.app.job.a00100.crawl.job.request.process.rakuten.query;

import java.util.ArrayList;
import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import a00100.app.job.a00100.crawl.Connection;
import a00100.app.job.a00100.crawl.job.webBrowser.WebClient;
import common.jdbc.JDBCUtils;
import lombok.val;
import lombok.experimental.Accessors;

@Accessors(prefix = "m_", chain = false)
class Plan extends WebClient {
	static final ThreadLocal<Plan> m_instances = new ThreadLocal<Plan>() {
		@Override
		protected Plan initialValue() {
			return new Plan();
		}
	};
	_Current m_current;

	Plan() {
	}

	static Plan getInstance() {
		return m_instances.get();
	}

	static _Current getCurrent() {
		return getInstance().m_current;
	}

	public WebClient execute() throws Exception {
		try {
			insert();
			return null;
		} finally {
			m_instances.remove();
		}
	}

	void insert() throws Exception {
		val conn = Connection.getCurrent().getDefault();
		try (val stmt = JDBCUtils.createStatement(conn)) {
			String sql;
			sql = "WITH s_params AS\n"
				+ "(\n"
					+ "SELECT ?::VARCHAR AS hotel_code,\n"
						+ "?::VARCHAR AS hotel_name,\n"
						+ "?::VARCHAR AS plan_code,\n"
						+ "?::VARCHAR AS plan_name,\n"
						+ "?::VARCHAR AS room_code,\n"
						+ "?::VARCHAR AS room_name\n"
				+ ")\n"
				+ "INSERT INTO t_price_rakuten\n"
				+ "(\n"
					+ "hotel_code,\n"
					+ "hotel_name,\n"
					+ "plan_code,\n"
					+ "plan_name,\n"
					+ "room_code,\n"
					+ "room_name\n"
				+ ")\n"
				+ "SELECT t10.hotel_code,\n"
					+ "t10.hotel_name,\n"
					+ "t10.plan_code,\n"
					+ "t10.plan_name,\n"
					+ "t10.room_code,\n"
					+ "t10.room_name\n"
				+ "FROM s_params AS t10\n";

			stmt.parse(sql);

			for (val r : query()) {
				int colNum = 1;
				stmt.setString(colNum++, r.getHotelCode());
				stmt.setString(colNum++, null);
				stmt.setString(colNum++, r.getPlanCode());
				stmt.setString(colNum++, null);
				stmt.setString(colNum++, r.getRoomCode());
				stmt.setString(colNum++, null);
				stmt.addBatch();
			}

			stmt.executeBatchAndClear();
			JDBCUtils.commit(conn);
		}
	}

	Collection<_00000> query() throws Exception {
		return new ArrayList<_00000>() {
			{
				val driver = getWebDriver();
				val actions = new Actions(driver);
				val by = By.xpath("//ul[contains(@class,'htlPlnRmTypLst')]/li");

				for (val element : driver.findElements(by)) {
					add(new _00000() {
						{
							// -------------------------------------------------------
							// プランまでスクロールしないと金額が取得できない
							// -------------------------------------------------------
							actions.moveToElement(m_element = element);
							actions.perform();
						}
					});
				}
			}
		};
	}

	static class _Current extends WebClient {
	}

	static class _00000 extends _Current {
		String m_hotelCode;
		String m_planCode;
		String m_roomCode;
		String m_roomName;
		String m_roomInfo;
		String m_roomRemark;
		String m_roomMeal;
		String m_roomPeople;
		String m_roomPayment;
		String m_price;
		WebElement m_element;

		String getHotelCode() throws Exception {
			if (m_hotelCode == null) {
				val by = By.name("f_no");
				val element = m_element.findElement(by);
				m_hotelCode = element.getAttribute("value");
			}

			return m_hotelCode;
		}

		String getPlanCode() throws Exception {
			if (m_planCode == null) {
				val by = By.name("f_camp_id");
				val element = m_element.findElement(by);
				m_planCode = element.getAttribute("value");
			}

			return m_planCode;
		}

		String getRoomCode() throws Exception {
			if (m_roomCode == null) {
				val by = By.name("f_syu");
				val element = m_element.findElement(by);
				m_roomCode = element.getAttribute("value");
			}

			return m_roomCode;
		}

		String getRoomName() throws Exception {
			if (m_roomName == null) {
				val by = By.xpath(".//*[@data-locate='roomType-name']");
				val element = m_element.findElement(by);
				m_roomName = element.getText();
			}

			return m_roomName;
		}

		String getRoomInfo() throws Exception {
			if (m_roomInfo == null) {
				val by = By.xpath(".//*[@data-locate='roomType-Info']");
				val element = m_element.findElement(by);
				m_roomInfo = element.getText();
			}

			return m_roomInfo;
		}

		String getRoomRemark() throws Exception {
			if (m_roomRemark == null) {
				val by = By.xpath(".//*[@data-locate='roomType-Remark']");
				val element = m_element.findElement(by);
				m_roomRemark = element.getText();
			}

			return m_roomRemark;
		}

		String getRoomMeal() throws Exception {
			if (m_roomMeal == null) {
				val by = By.xpath(".//*[@data-locate='roomType-option-meal']");
				val element = m_element.findElement(by);
				m_roomMeal = element.getText();
			}

			return m_roomMeal;
		}

		String getRoomPeople() throws Exception {
			if (m_roomPeople == null) {
				val by = By.xpath(".//*[@data-locate='roomType-option-people']");
				val element = m_element.findElement(by);
				m_roomPeople = element.getText();
			}

			return m_roomPeople;
		}

		String getRoomPayment() throws Exception {
			if (m_roomPayment == null) {
				val by = By.xpath(".//*[@data-locate='roomType-option-payment']");
				val element = m_element.findElement(by);
				m_roomPayment = element.getText();
			}

			return m_roomPayment;
		}

		String getPrice() throws Exception {
			if (m_price == null) {
				val by = By.xpath(".//*[contains(@class,'originalPrice')]");
				val element = m_element.findElement(by);
				m_price = element.getText();
			}

			return m_price;
		}

		@Override
		public WebClient submit() throws Exception {
			//getPrice();
			return null;
		}
	}
}