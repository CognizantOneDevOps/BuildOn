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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cognizant.buildon.domain.Constants;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class PreferenceServlet
 */
@WebServlet("/PreferenceController")
public class PreferenceController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public PreferenceController() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String userid=request.getParameter("userId");
		String type=request.getParameter("type");
		List<String> list=new ArrayList<String>();
		
		String userId=null;
		String responseStr=null;
		String globalCookie=null;
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
		 list=buildonservice.getPeferenceDetails(userinfo.getEmail(),type);
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
		boolean status=false;
		String userid=request.getParameter("userId");
		String switchmode=request.getParameter("switchmode");
		String repo=request.getParameter("repo");
		String branch=request.getParameter("branch");
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
		status=buildonservice.savePeferenceDetails(userid,switchmode,repo,branch);
		 responseStr=String.valueOf(status);
		}else{
			buildonservice.deleteCookies(response, cookie);
			responseStr=Constants.INVALID;
		}
		response.getWriter().write(responseStr);
		
	}
	
}
