package a00100.app.job.a00100.rakuten.job.request.process.crawl.query;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import a00100.app.job.a00100.rakuten.job.request.process.Process;
import a00100.app.job.a00100.rakuten.job.request.process.webBrowser.WebClient;
import common.lang.time.DateFormatUtils;
import lombok.val;
import lombok.experimental.Accessors;

@Accessors(prefix = "m_", chain = false)
public class Query extends WebClient {
	static final ThreadLocal<Query> m_instances = new ThreadLocal<Query>() {
		@Override
		protected Query initialValue() {
			return new Query();
		}
	};

	Query() {
	}

	public static Query getInstance() {
		return m_instances.get();
	}

	@Override
	public WebClient execute() throws Exception {
		try {
			for (WebClient client = new _00000(); client != null;) {
				client = client.execute();
			}

			return next();
		} finally {
			m_instances.remove();
		}
	}

	WebClient next() {
		return Load.getInstance();
	}

	static class _00000 extends WebClient {
		@Override
		public void navigate() throws Exception {
			val URL = "https://hotel.travel.rakuten.co.jp/hotelinfo/plan/%s";
			val process = Process.getCurrent();
			val driver = getDriver();
			driver.get(String.format(URL, process.getHotelCode()));
		}

		@Override
		public WebClient submit() throws Exception {
			return new _00100();
		}
	}

	static class _00100 extends WebClient {
		@Override
		public WebClient submit() throws Exception {
			pushDate();
			setCheckin();
			setCheckout();
			setRoom();
			setAdult();
			setChild();
			pushQuery();
			return null;
		}

		void pushDate() throws Exception {
			// --------------------------------------------------
			// 【日付未定】チェックを外す
			// --------------------------------------------------
			val driver = getDriver();
			val by = By.id("dh-unspecified-date");
			val element = driver.findElement(by);

			if (element.isSelected() == true) {
				element.click();
			}
		}

		void setCheckin() throws Exception {
			// --------------------------------------------------
			// チェックイン
			// --------------------------------------------------
			val process = Process.getCurrent();
			val driver = getDriver();
			val by = By.id("dh-checkin");
			val element = driver.findElement(by);

			element.sendKeys(Keys.ESCAPE);
			element.sendKeys(Keys.CONTROL, "a");
			element.sendKeys(Keys.DELETE);
			element.sendKeys(DateFormatUtils.DATE_NO_T_FORMAT.format(process.getCheckInDate()));
		}

		void setCheckout() throws Exception {
			// --------------------------------------------------
			// チェックアウト
			// --------------------------------------------------
			val process = Process.getCurrent();
			val driver = getDriver();
			val by = By.id("dh-checkout");
			val element = driver.findElement(by);

			element.sendKeys(Keys.ESCAPE);
			element.sendKeys(Keys.CONTROL, "a");
			element.sendKeys(Keys.DELETE);
			element.sendKeys(DateFormatUtils.DATE_NO_T_FORMAT.format(process.getCheckOutDate()));
		}

		void setRoom() throws Exception {
			// --------------------------------------------------
			// ご利用部屋数
			// --------------------------------------------------
			val process = Process.getCurrent();
			if (process.getRoomNums() != null) {
				val driver = getDriver();
				val by = By.id("dh-room");
				val element = new Select(driver.findElement(by));

				element.selectByValue(String.format("%d", process.getRoomNums()));
			}
		}

		void setAdult() throws Exception {
			// --------------------------------------------------
			// 大人人数
			// --------------------------------------------------
			val process = Process.getCurrent();
			if (process.getAdultNums() != null) {
				val driver = getDriver();
				val by = By.id("dhAdult1");
				val element = new Select(driver.findElement(by));

				element.selectByValue(String.format("%d", process.getAdultNums()));
			}
		}

		void setChild() throws Exception {
			// --------------------------------------------------
			// 子供人数
			// --------------------------------------------------
			val driver = getDriver();
			val by = By.id("chldNum1");
			val element = driver.findElement(by);

			element.click();
			setUpperGrade();
			setLowerGrade();
			element.click();
		}

		void setUpperGrade() throws Exception {
			// --------------------------------------------------
			// 高学年
			// --------------------------------------------------
			val process = Process.getCurrent();
			if (process.getUpperGradeNums() != null) {
				val driver = getDriver();
				val by = By.id("dh-s1");
				val element = new Select(driver.findElement(by));

				element.selectByValue(String.format("%d", process.getUpperGradeNums()));
			}
		}

		void setLowerGrade() throws Exception {
			// --------------------------------------------------
			// 低学年
			// --------------------------------------------------
			val process = Process.getCurrent();
			if (process.getLowerGradeNums() != null) {
				val driver = getDriver();
				val by = By.id("dh-s2");
				val element = new Select(driver.findElement(by));

				element.selectByValue(String.format("%d", process.getLowerGradeNums()));
			}
		}

		void pushQuery() throws Exception {
			// --------------------------------------------------
			// 【検索】押下
			// --------------------------------------------------
			val driver = getDriver();
			val by = By.id("dh-submit");
			val element = driver.findElement(by);

			element.click();
		}
	}
}
