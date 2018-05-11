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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.services.BuildOnService;
import com.cognizant.buildon.services.BuildOnServiceImpl;

/**
 * @author 338143
 *
 */
public class QuartzJob  implements Job{

	private static final Logger logger=LoggerFactory.getLogger(QuartzJob.class);
	private String commitid=null;
	private String logdir=null;
	private File file=null;
	private  Connection con=null;
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		HashMap<String,String> map=new HashMap<String,String>();
		
		//Postgres Query to remove record older than 15 days
		//select commitid,logdir,DATE(start_timestamp)  from buildon_reports where DATE(start_timestamp) < ( SELECT CURRENT_DATE + INTERVAL '-15 day' )
		String  sql = ("select commitid,logdir,DATE(start_timestamp)  from buildon_reports where DATE(start_timestamp) < ( SELECT CURRENT_DATE + INTERVAL '-30 day' )");
		con=createConnection();
		try(PreparedStatement statement=con.prepareStatement(sql)) {
			try( ResultSet rs = statement.executeQuery()){
				while (rs.next()) {
					commitid=rs.getString(1);
					logdir=rs.getString(2);
					map.put(commitid,logdir);
					logger.info("commitid: "+commitid+" logdir: "+logdir);					
				}
			}			
			
		} catch (SQLException e) {
			logger.debug(e.toString());
		}
		for(Entry<String,String> e: map.entrySet()){
			if(null!=e.getValue() && !e.getValue().equals("")){
				//file=new File(logdir);
				//delete(file);
				//deleteReportsRec(e.getKey().toString());
				logger.info("commitid: "+e.getKey().toString()+" logdir: "+e.getValue().toString());
				file=new File(e.getValue().toString());
				delete(file);
				deleteReportsRec(e.getKey().toString());
			}
		}
	}

	
	/**
	 * @param file
	 */
	public static void delete(File file){
		File fileDelete =null;
		if(file.isDirectory()){
			if(file.list().length==0){

				file.delete();
			}else{
				String files[] = file.list();
				for (String temp : files) {
					fileDelete=new File(file, temp);
					delete(fileDelete);
				}
				if(file.list().length==0){
					file.delete();

				}
			}

		}else{
			file.delete();
		}
	}

	/**
	 * @param commitid
	 */
	private void deleteReportsRec(String commitid) {
		String sql="delete from buildon_reports where commitid=?";
		con=createConnection();
		try(PreparedStatement statement=con.prepareStatement(sql)) {
			statement.setString(1,commitid);
			statement.executeUpdate();
			logger.info("commitid "+commitid+" row has been deleted");			
		} catch (SQLException e) {
			logger.debug(e.toString());
		}		
	}

	private  Connection  createConnection(){
		Properties props = readPropertyFile();
		String driver = props.getProperty("postgresql.driver");
		String url = props.getProperty("postgresql.url");
		String username = props.getProperty("postgresql.username");
		String password = props.getProperty("postgresql.password");
	    //String pass=service.decrypt(password);
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		try {
			con = dataSource.getConnection();
		} catch (SQLException e) {
			logger.debug(e.toString());
		}
		
		return con;
	}

	private  Properties readPropertyFile() {
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
		return props;
	}

}
