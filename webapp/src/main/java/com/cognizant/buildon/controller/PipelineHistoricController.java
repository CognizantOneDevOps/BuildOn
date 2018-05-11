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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognizant.buildon.domain.GitOperations;
import com.cognizant.buildon.services.BuildOnFactory;
import com.cognizant.buildon.services.BuildOnService;
import com.cognizant.buildon.services.BuildOnServiceImpl;

/**
 * Servlet implementation class IndividualReport
 */
/**
 * @author 338143
 *
 */
@WebServlet("/PipelineHistoricController")
public class PipelineHistoricController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static  final Logger logger=LoggerFactory.getLogger(PipelineHistoricController.class);


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PipelineHistoricController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService buildonservice=BuildOnFactory.getInstance();
		String commitid=request.getParameter("commitid");
		logger.debug("commitid pipeline"+commitid);
		String json=null;
		boolean isvalidId=buildonservice.isAlphaNumeric(commitid);
		if(isvalidId){
			json=buildonservice.getJsonData(commitid);
		}
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BuildOnService service=new BuildOnServiceImpl();
		String commitid=request.getParameter("commitid");
		logger.debug("commitid pipeline"+commitid);
		boolean updateStatus=true;
		boolean isvalidId=service.isAlphaNumeric(commitid);
		if(isvalidId){
			updateStatus=GitOperations.getHistoricDBService(commitid);
		}
		String res=String.valueOf(updateStatus);
		response.getWriter().write(res);
	}

}
