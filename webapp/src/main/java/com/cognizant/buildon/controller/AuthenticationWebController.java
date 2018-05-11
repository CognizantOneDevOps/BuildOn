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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.domain.LDAPAuthentication;
import com.cognizant.buildon.domain.Users;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;
import com.google.gson.Gson;

/**
 * @author 338143
 *
 */

/**
 * Servlet implementation class Authentication
 */
@WebServlet("/AuthenticationWebController")
public class AuthenticationWebController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AuthenticationWebController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService service=BuildOnFactory.getInstance();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String decodedpass=service.decrypt(password);
		String userId=null;
		String userid=null;
		userid=String.valueOf(userId);
		Cookie cookie=new Cookie("user",service.encrypt(userid));
		cookie.setMaxAge(54000);
		response.addCookie(cookie);
		response.getWriter().write(username);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService service=BuildOnFactory.getInstance();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		Properties props = service.readPropertyFile();
		boolean isLDAP = Boolean.parseBoolean(props.getProperty("ldap.isLDAP"));
		Users user=new Users();
		String userId=null;
		List<Users> users=new ArrayList<Users>();
		String json=null;
		if(!isLDAP){
		users=service.getAuth(username,password);
		if(!users.isEmpty()){
			user=users.get(0);
		}
		userId=String.valueOf(user.getId());
		Cookie cookie=new Cookie("user",service.encrypt(userId));
		cookie.setMaxAge(54000);
		response.addCookie(cookie);
		json = new Gson().toJson(users);
		}else{
			String userid=null;
			user=LDAPAuthentication.getEmpId(username,password);
			userid=String.valueOf(user.getId());
			Cookie cookie=new Cookie("user",service.encrypt(userid));
			cookie.setMaxAge(54000);
			response.addCookie(cookie);
			users.add(user);
			json = new Gson().toJson(users);
		}
		response.getWriter().write(json);

	}
	
}
