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

package com.cognizant.buildon.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.Reports;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/SearchController")
public class SearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(SearchController.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userid=request.getParameter("userId");
		String globalCookie=null;
		String userId=null;
		String responseStr=null;
		Cookie[] cookie =request.getCookies();
		if(cookie!=null ){
			for (Cookie cookies : cookie) {
				if (cookies.getName().equals("user")) {
				globalCookie= cookies.getValue();
				}
			}
		}
		userId=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userId && !(userId.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userId);
			JSONObject list=buildonservice.getHistoricalReports(userinfo.getEmail());
			response.getWriter().write(list.toString());

		}else{
			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
			response.getWriter().write(responseStr.toString());

		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		List<Reports> list=new ArrayList<>();

		String userId=request.getParameter("userId");
		String project=request.getParameter("project");
		String branch=request.getParameter("branch");
		String intiatedBy=request.getParameter("intiatedby");
		String srtdate=request.getParameter("sdate");
		String enddate=request.getParameter("edate");
		JSONObject json=null;
		JSONArray jsArray = new JSONArray();
		String calduration=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate=new Date();
		Date endDate=new Date();
		try {
			startDate = sdf.parse(srtdate);
			endDate=sdf.parse(enddate);
		} catch (ParseException e1) {
			logger.debug(e1.toString());
		}

		String globalCookie=null;
		String responseStr=null;
		Cookie[] cookie =request.getCookies();
		if(cookie!=null ){
			for (Cookie cookies : cookie) {
				
				if (cookies.getName().equals("user")) {
				globalCookie= cookies.getValue();
				}
			}
		}
		userId=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userId && !(userId.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userId);
			list=buildonservice.getresults(startDate,endDate,project,branch,intiatedBy,userinfo.getEmail());
			for(Reports report:list){
				json=new JSONObject();
				try {
					json.put("jobname",report.getJobname());
					json.put("status",report.getStatus());
					json.put("project", report.getProject());
					json.put("branch", report.getBranch());
					calduration = calculateDuration(calduration, report);
					json.put("duration",calduration);
					json.put("commitid", report.getCommitid());
					json.put("TRIGGER_FROM",report.getTRIGGER_FROM());
					json.put("scmuser",report.getScmuser());
					jsArray.put(json);
				} catch (JSONException e) {
					logger.debug(e.toString());
				}
			}
			response.getWriter().write(jsArray.toString());

		}else{
			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
			response.getWriter().write(responseStr.toString());


		}

	}

	private String calculateDuration(String calduration, Reports report) {
		if( report.getDuration()!=null){
			int minutes = report.getDuration() / 60000;
			int minutes1 = report.getDuration() % 60000;
			int seconds = minutes1 / 1000;
			calduration = minutes + "Mins " + seconds + "Sec"; 
		}
		return calduration;
	}


}
