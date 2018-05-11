/*******************************************************************************
* Copyright 2018 Cognizant Technology Solutions
*  
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not
*  use this file except in compliance with the License.  You may obtain a copy
*  of the License at
*  
*    http://www.apache.org/licenses/LICENSE-2.0
*  
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
*  License for the specific language governing permissions and limitations under
*  the License.
 ******************************************************************************/

package com.cognizant.buildon.scheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author 338143
 *
 */

@WebListener
public class QuartzListener implements ServletContextListener {
	private static final Logger logger=LoggerFactory.getLogger(QuartzListener.class);
	private Scheduler scheduler = null;
	/**
	 * Default constructor. 
	 */
	public QuartzListener() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0)  { 
		// TODO Auto-generated method stub
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0)  { 
		Properties props = new Properties();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("buildon.properties");
		try {
			props.load(is);
			is.close();
		} catch (FileNotFoundException e1) {
			logger.debug(e1.toString());
		} catch (IOException e) {
			logger.debug(e.toString());
		}
		String  crontime= props.getProperty("cronschedule");
		try {
			JobDetail job = newJob(QuartzJob.class).withIdentity("CronQuartzJob", "Group").build();
			Trigger trigger = newTrigger().withIdentity("TriggerName", "Group")
							.withSchedule(CronScheduleBuilder.cronSchedule(crontime))
							.build();
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(job, trigger);
		}
		catch (SchedulerException e) {
			logger.debug(e.toString());
		}
	}

}
