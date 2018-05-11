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

angular.module('Authentication')

.controller('LoginController',
		['$scope', '$rootScope', '$location','$http','$localStorage','$route',
		 function ($scope, $rootScope, $location,$http,$localStorage,$route) {

			$scope.showThrobber = false;
			$scope.login = function () {
				console.log("Authentication");
				$scope.showThrobber = true;
				var data = $.param({
					username:$scope.username,
					password:$scope.password 
				});
				var config = {
						headers : {
							'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
						}
				}
				$http.post('AuthenticationWebController',data,config)
				.then(function (response) {				
					console.log("response"+response.data);
					$scope.results=response.data;
					if($scope.results.length >0  && $scope.results[0].email != 'noLDAPUser'){
						$scope.isLoginError=false;

						$rootScope.scmUser=$scope.results[0].email;
						console.log("$rootScope.scmUser:"+$rootScope.scmUser);
						$rootScope.name=$scope.results[0].uname;
						$location.path('/home');
					}else {
						$scope.error = response.message;
						$scope.showThrobber = false;
						$scope.isLoginError=true;
						$scope.errormsg="Invalid credentials";
					}
				},
				function(error) {
					console.log("error");
				});
			} 

		}]);
