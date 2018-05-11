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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.dao.BuildOnDAOImpl;
import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.ScmDetails;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;
import com.google.gson.Gson;


/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class ScmUserDetailsServlet
 */
@WebServlet("/ScmUserDetailsController")
public class ScmUserDetailsController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ScmUserDetailsController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String id=request.getParameter("id");
		String userid=request.getParameter("userid");
		String responseStr=null;
		Cookie[] cookie =request.getCookies();
		String globalCookie=null;
		if(cookie!=null ){
			for (Cookie cookies : cookie) {
				if (cookies.getName().equals("user")) {
					globalCookie= cookies.getValue();
				}
			}
		}
		String userId=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userId && !(userId.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userId);
			boolean status=buildonservice.removeRecord(id,userid);
			responseStr=String.valueOf(status);
		}else{

			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
		}
		response.getWriter().write(responseStr);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userid=request.getParameter("userId");
		String type=request.getParameter("type");
		
		String json =null;

		Cookie[] cookie =request.getCookies();
		String globalCookie=null;
		if(cookie!=null ){
			for (Cookie cookies : cookie) {
				if (cookies.getName().equals("user")) {
					globalCookie= cookies.getValue();
				}
			}
		}

		String userId=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userId && !(userId.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userId);
			List<ScmDetails> scmdet=buildonservice.getScmDetails(userinfo.getEmail(),type);
			json = new Gson().toJson(scmdet);

		}else{
			buildonservice.deleteCookies(response, cookie);
			json=Constants.INVALID;
		}
		response.getWriter().write(json);


	}



}
