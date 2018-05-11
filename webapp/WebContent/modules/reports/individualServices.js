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

'use strict';

angular.module('Individual')

.factory('IndividualService',
		['Base64', '$http', '$cookieStore', '$rootScope', '$timeout',
		 function (Base64, $http, $cookieStore, $rootScope, $timeout) {

			var service = {};

			service.getIndividualReports = function (scmuser,callback) {
				var response =$http({
					url : 'ReportController',
					method: "GET",
					params: {
						"scmuser":scmuser
					}
				})
				.then(function successCallback(response,status) {
					callback(response);
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				return response;

			};
			
			service.getIndividualStatusReports = function (scmuser,commitid,callback) {
				var response =$http({
					url : 'ReportController',
					method: "POST",
					params: {
						"scmuser":scmuser,
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
			
			service.getJsonData = function (commitid,callback) {
				var response =$http({
					url : 'IndividualReportController',
					method: "GET",
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
			
			service.getDBServiceUpdate = function (commitid,repo,branch,callback) {
				var response =$http({
					url : 'IndividualReportController',
					method: "POST",
					params: {
						"commitid":commitid,
						"repo":repo,
						"branch":branch
					}
				})
				.then(function successCallback(response,status) {
					callback(response);
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				return response;

			};
			
			

			

			
			return service;	
		}]);//End of factory
