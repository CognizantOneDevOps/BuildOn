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

angular.module('Preference')

.factory('PreferenceService',
		['Base64', '$http', '$cookieStore', '$rootScope', '$timeout',
		 function (Base64, $http, $cookieStore, $rootScope, $timeout) {
			
			var service = {};
			service.getPreferenceDetails = function (userId,type,callback) {
			var response =$http({
				url : 'PreferenceController',
				method: "GET",
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
			
			};
			
		service.savePreferenceDetails = function (userId,switchmode,repo,branch,callback) {
				var response =$http({
					url : 'PreferenceController',
					method: "POST",
					params: {
						"userId":userId,
						"switchmode":switchmode,
						"repo":repo,
						"branch":branch
					}
				})
				.then(function successCallback(response,status) {		
					callback(response); 
				}, function errorCallback (response,status) {
					callback(response);
				});
				
				};
			

			
			
			return service;	
		}]);//End of factory
