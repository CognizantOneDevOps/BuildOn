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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class ReportServlet
 */
@WebServlet("/ReportController")
public class ReportController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	  
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReportController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String scmuser=request.getParameter("scmuser");
		JSONObject json=null;
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
		json=buildonservice.getIndividualReports(scmuser);
		response.getWriter().write(json.toString());
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
		String scmuser=request.getParameter("scmuser");
		String commitid=request.getParameter("commitid");
		JSONObject json=null;
		String responseStr=null;
		String globalCookie=null;
		String userId=null;
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
		json=buildonservice.getIndividualstatusReports(scmuser,commitid);
		response.getWriter().write(json.toString());
		}else{
			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
			response.getWriter().write(responseStr.toString());
		}

		

	}
	
	

}
