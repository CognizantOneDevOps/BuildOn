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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Servlet implementation class BuildonServlet
 */
@WebServlet("/BuildonWebController")
public class BuildonWebController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static  final Logger logger=LoggerFactory.getLogger(BuildonWebController.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public BuildonWebController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		HttpSession session=request.getSession();
		String userId=request.getParameter("userId");
		String branch=request.getParameter("branch");
		String repo=request.getParameter("repo");
		String type=request.getParameter("type");
		boolean status=false;
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
		String userid=buildonservice.getCookiesDecrytpedvalue(globalCookie);
		if(null!=userid && !(userid.equals(""))){
			Users userinfo=buildonservice.getEmailForUser(userid);
			try {
				status = GitOperations.callBuildon(userinfo.getEmail(),branch,repo,session,type);
				responseStr=String.valueOf(status);
			} catch (InvalidRemoteException e) {
				logger.debug(e.toString());
			} catch (TransportException e) {
				logger.debug(e.toString());
			} catch (GitAPIException e) {
				logger.debug(e.toString());
			}



		}else{
			if (cookie != null) {
				for (Cookie cookiedel : cookie) {
					cookiedel.setValue(null);
					cookiedel.setMaxAge(0);
					response.addCookie(cookiedel);

				}
			}
			responseStr=Constants.INVALID;
		}

		response.getWriter().write(responseStr);
	}

}
