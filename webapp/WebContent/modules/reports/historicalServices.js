/*******************************************************************************
*Copyright 2018 Cognizant Technology Solutions
* 
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.  You may obtain a copy
* of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
* License for the specific language governing permissions and limitations under
* the License.
 ******************************************************************************/

/**
 * 
 */
'use strict';

angular.module('Historical')

.factory('HistoricalService',
		['Base64', '$http', '$cookieStore', '$rootScope', '$timeout',
		 function (Base64, $http, $cookieStore, $rootScope, $timeout) {
			
			var service = {};
			service.getHistoricalReports = function (userId,callback) {
			var response =$http({
				url : 'SearchController',
				method: "GET",
				params: {
					"userId":userId
				}
			})
			.then(function successCallback(response,status) {		
				callback(response); 
			}, function errorCallback (response,status) {
				callback(response);
			});
			
			};			
			
	
			
			service.getReportTriggerData = function (commitid,callback) {
				var response =$http({
					url : 'HistoricWebController',
					method: "POST",
					params: {
						"commitid":commitid						
					}
				})
				.then(function successCallback(response,status) {
					callback(response);
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				return response;

			};
			
			service.getHistoricDBService = function (commitid,callback) {
				var response =$http({
					url : 'PipelineHistoricController',
					method: "POST",
					params: {
						"commitid":commitid						
					}
				})
				.then(function successCallback(response,status) {
					callback(response);
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				return response;

			};
			
			service.getSearchresult = function (userId,project,intiatedby,sdate,edate,callback) {
				var response =$http({
					url : 'SearchController',
					method: "POST",
					params: {
						"userId":userId,
						"project":project,					
						"intiatedby":intiatedby,
						"sdate":sdate,
						"edate":edate
					}
				})
				.then(function successCallback(response,status) {		
					callback(response); 
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				};
				
				service.getBranchDetails = function (userId,repo,callback) {
					var response =$http({
						url : 'HistoricCIWebController',
						method: "POST",
						params: {
							"userId":userId,
							"repo":repo
						}
					})
					.then(function successCallback(response,status) {		
						callback(response); 
					}, function errorCallback (response,status) {
						callback(response);
					});
						
				 }

				
			
			return service;	
		}]);//End of factory
