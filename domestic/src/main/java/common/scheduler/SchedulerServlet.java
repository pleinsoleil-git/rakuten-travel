package common.scheduler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.xml.XMLSchedulingDataProcessor;

import lombok.val;

public class SchedulerServlet extends HttpServlet {
	@Override
	public void init() throws ServletException {
		try {
			val l = new CascadingClassLoadHelper();
			val s = StdSchedulerFactory.getDefaultScheduler();
			val p = new XMLSchedulingDataProcessor(l);

			p.processFileAndScheduleJobs("quartz.xml", s);
			s.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
