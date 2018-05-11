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

angular.module('Home')

.factory('HomeService',
		['Base64', '$http', '$cookieStore', '$rootScope', '$timeout',
		 function (Base64, $http, $cookieStore, $rootScope, $timeout) {
			var service = {};
			service.save = function (userId,switchval,type,url,oauthtoken,id, callback) {
			var response =$http({
				url : 'ScmPersistController',
				method: "POST",
				params: {
					"userId":userId,
					"switchval":switchval,
					"type":type,
					"url":url,
					"oauthtoken": oauthtoken, 
					"id":id
				}
			})
			.then(function successCallback(response,status) {		
				callback(response); 
			}, function errorCallback (response,status) {
				callback(response);
			});
			
			};
			
		 service.getScmDetails = function (userId,type,callback) {
					var response =$http({
						url : 'ScmUserDetailsController',
						method: "POST",
						params: {
							"userId":userId,
							"type":type
						}
					})
					.then(function successCallback(response,status) {
						callback(response); 
					}, function errorCallback (response,status) {
						callback(response);
					});
					
		}
		 
		 service.getTestCon = function (uname,uurl, callback) {
				var response =$http({
					url : 'ScmPersistController',
					method: "GET",
					params: {
						"user":uname,
						"url" :uurl
					}
				})
				.then(function successCallback(response,status) {		
					callback(response); 
				}, function errorCallback (response,status) {
					callback(response);
				});
				
		 }
		 
		 service.removeRecord = function (id,userid, callback) {
				var response =$http({
					url : 'ScmUserDetailsController',
					method: "GET",
					params: {
						"id":id,
						"userid":userid
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
