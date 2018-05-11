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
import javax.servlet.http.HttpSession;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.GitOperations;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class JenkinsServlet
 */

@WebServlet("/JenkinsWebController")
public class JenkinsWebController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JenkinsWebController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*HttpSession session=request.getSession();
		String userId=request.getParameter("userId");
		String content=request.getParameter("content");
		String branch=request.getParameter("branch");
		String repo=request.getParameter("repo");*/
		boolean isSaved=false;
		//GitOperations.buildon(userId,branch,repo,content,session);
		String resp=String.valueOf(isSaved);
		response.getWriter().write(resp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		HttpSession session=request.getSession();
		String userid=request.getParameter("userId");
		String content=request.getParameter("content");
		String type=request.getParameter("type");
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
			boolean isSaved=GitOperations.saveJenkinsEdit(userinfo.getEmail(),content,session,type);
			responseStr=String.valueOf(isSaved);
			response.getWriter().write(responseStr);
		}else{
			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
			response.getWriter().write(responseStr);
		}

	}

}
