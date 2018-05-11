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

package com.cognizant.buildon.services;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cognizant.buildon.domain.Reports;
import com.cognizant.buildon.domain.ScmDetails;
import com.cognizant.buildon.domain.Service;
import com.cognizant.buildon.domain.Users;

/**
 * @author 338143
 *
 */

public interface BuildOnService {

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	public List<Users> getAuth(String username, String password) ;

	/**
	 * @param startdate
	 * @param enddate
	 * @param project
	 * @param branch
	 * @param intiatedBy
	 * @param userId
	 * @return
	 */
	public List<Reports> getresults(Date startdate,Date enddate,String project,String branch,String intiatedBy,String userId);


	/**
	 * @param userId
	 * @param switchval
	 * @param type
	 * @param url
	 * @param username
	 * @param id
	 * @return
	 */
	public boolean  saveScmDet(String userId,String switchval,String type,String url ,String username,String id) ;

	/**
	 * @param userid
	 * @param type
	 * @return
	 */
	public  List<ScmDetails> getScmDetails(String userid,String type);

	/**
	 * @param id
	 * @param userid 
	 * @return
	 */
	public boolean removeRecord(String id, String userid);

	/**
	 * @param userid
	 * @param type
	 * @return
	 */
	public List<String> getPeferenceDetails(String userid, String type);

	/**
	 * @param userid
	 * @param switchmode
	 * @param repo
	 * @param branch
	 * @return
	 */
	public boolean savePeferenceDetails(String userid, String switchmode, String repo,String branch);

	/**
	 * @param userid
	 * @return
	 */
	public JSONObject getHistoricalReports(String userid);

	/**
	 * @param scmuser
	 * @return
	 */
	public JSONObject getIndividualReports(String scmuser);

	/**
	 * @param scmuser
	 * @param commitid
	 * @return
	 */
	public JSONObject getIndividualstatusReports(String scmuser, String commitid);

	/**
	 * @param commitid
	 * @return
	 */
	public String getJsonData(String commitid);

	/**
	 * @param commitId
	 * @return
	 */
	public String getReportTriggerData(String commitId);

	/**
	 * @param password
	 * @return
	 */
	public String decrypt(String password);

	/**
	 * @param value
	 * @return
	 */
	public  boolean isNumeric(String value); 

	/**
	 * @param value
	 * @return
	 */
	public  boolean isAlphaNumeric(String value);

	/**
	 * @param value
	 * @return
	 */
	public  boolean isValidUrl(String value); 

	/**
	 * @param value
	 * @return
	 */
	public  boolean isValidEmail(String value);

	/**
	 * @param value
	 * @return
	 */
	public boolean isValidAlpha(String value);

	/**
	 * @return
	 */
	public String getEncryptkey();



	/**
	 * @param value
	 * @return
	 */
	public String encrypt(String value);



	/**
	 * @param globalCookie
	 * @return
	 */
	public String getCookiesDecrytpedvalue(String globalCookie);

	/**
	 * @param id
	 * @return
	 */
	public Users  getEmailForUser(String id);

	/**
	 * @return
	 */
	public Properties readPropertyFile();

	/**
	 * @param response
	 * @param cookie
	 */
	public void deleteCookies(HttpServletResponse response, Cookie[] cookie);


	/**
	 * @param userId
	 * @return
	 */
	public  JSONArray getCompareBuild(String userId);

	/**
	 * @param userId
	 * @return
	 */
	public  JSONArray getLatestbuild(String userId);

	/**
	 * @param userId
	 * @return
	 */
	public  JSONArray getProjectwiseBuild(String userId) ;

	/**
	 * @param userId
	 * @return
	 */
	public  JSONArray getBuildtrends(String userId);
	
	/**
	 * @param email
	 * @param type
	 * @return
	 */
	public   List<ScmDetails> getUserScmDetails(String email ,String type);
	
	/**
	 * @param podIP
	 * @param podPort
	 * @param podNameValue
	 * @param resultJSON
	 * @param commitid
	 * @return
	 */
	public boolean getDBServiceInsert(String podIP, String podPort, String podNameValue, String resultJSON,
			String commitid); 
	
	 /**
	 * @param podIP
	 * @param podPort
	 * @param podNameValue
	 * @param resultJSON
	 * @param commitid
	 * @return
	 */
	boolean getDBServiceUpdate(String podIP, String podPort, String podNameValue, String resultJSON,
				String commitid);


	 /**
	 * @param commitId
	 * @return
	 */
	public String getServiceCommitId(String commitId);
	
	/**
	 * @param commitId
	 * @param cijobname
	 * @return
	 */
	public String getReportsStatus(String commitId,String cijobname);
	
	
	 /**
	 * @param commitid
	 * @param resultJSON
	 * @return
	 */
	boolean getHistoricDBServiceInsert(String commitid,String resultJSON) ;
	
	/**
	 * @param commitId
	 * @return
	 */
	public  String getPodname(String commitId);
	
	/**
	 * @param commitId
	 * @return
	 */
	public Service getServiceData(String commitId);
	
	/**
	 * @param email
	 * @param repo
	 * @return
	 */
	List<ScmDetails> getHistoricalURL(String email, String repo);


}
