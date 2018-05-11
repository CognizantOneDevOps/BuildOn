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
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.GitOperations;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;
import com.google.gson.Gson;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class GithubServlet
 */
@WebServlet("/GithubWebController")
public class GithubWebController extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GithubWebController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * User project details /branch details
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userId=request.getParameter("userId");
		String repo=request.getParameter("repo");
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
		String userid=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userid && !(userid.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userid);
			ArrayList<String> listbranch=GitOperations.getBranchDetails(userinfo.getEmail(),repo,type);
			json = new Gson().toJson(listbranch);
		}else{
			buildonservice.deleteCookies(response, cookie);
			json="invalid";
		}
		response.getWriter().write(json);
	}

	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		HttpSession session=request.getSession();
		String project=request.getParameter("project");
		String branch=request.getParameter("branch");
		String userId=request.getParameter("userId");
		String type=request.getParameter("type");
		String content=null;
		
		Cookie[] cookie =request.getCookies();
		String globalCookie=null;
		if(cookie!=null ){
			for (Cookie cookies : cookie) {
				if (cookies.getName().equals("user")) {
				globalCookie= cookies.getValue();
				}
			}
		}
		String userid=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userid && !(userid.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userid);
			content=GitOperations.getJenkinsFile(project,branch,userinfo.getEmail(),session,type);
			if(content==null){
				content=Constants.NO_JENKINS;
			}
			
			response.getWriter().write(content);
			
		}else{
			buildonservice.deleteCookies(response, cookie);
			content=Constants.INVALID;
			response.getWriter().write(content);
		}
		
		
	}

}
