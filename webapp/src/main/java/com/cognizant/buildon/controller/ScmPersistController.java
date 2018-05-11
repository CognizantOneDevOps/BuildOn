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
 * Servlet implementation class ScmServlet
 */
@WebServlet("/ScmPersistController")
public class ScmPersistController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ScmPersistController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)-Testconnection 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user=request.getParameter("user");
		String url=request.getParameter("url");
		String responseStr =null;
		boolean isvalid=GitOperations.checkrepo(url,user);
		responseStr=String.valueOf(isvalid);
		response.getWriter().write(responseStr);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)-Save url
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		boolean isSaved=false;
		String userid=request.getParameter("userId");
		String switchval=request.getParameter("switchval");
		String type= request.getParameter("type");
		String url= request.getParameter("url");
		String oauthtoken=request.getParameter("oauthtoken");
		String id=request.getParameter("id");
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
			isSaved=buildonservice.saveScmDet(userinfo.getEmail(),switchval,type,url,oauthtoken,id);
			responseStr=String.valueOf(isSaved);
		}else{
			buildonservice.deleteCookies(response, cookie);
			 responseStr=Constants.INVALID;
		}
			
		response.getWriter().write(responseStr);
	}
	
	

}
