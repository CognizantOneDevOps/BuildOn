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

angular.module('Home')
.controller('scmController',
		['$scope','$timeout', '$location', 'HomeService','$rootScope','$http','$filter',
		 function ($scope,$timeout, $location,HomeService,$rootScope,$http,$filter) {
			$rootScope.shwfromhist=false;

			if(!$rootScope.type) {				
				$rootScope.type="first";
				$scope.loggedin = "first"; 
			}else {
				$scope.selection = { value: $rootScope.type };
				$scope.loggedin =" "; 
			}


			//$rootScope.userId =$rootScope.scmUser;
			console.log($rootScope.scmUser + $rootScope.type );

			var data = $.param({
				userId:$rootScope.scmUser,
				type:$rootScope.type
			});
			var config = {
					headers : {
						'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
					}
			}
			$http.post('ScmUserDetailsController',data,config)
			.then(function (response) {
				$scope.details=response.data;
				console.log("$scope.details.length....."+$scope.details.length);
				if($scope.details != "invalid"){

					var result = $filter('filter')($scope.details, {defaultvalue:1});		
					
					
					if(result.length > 0) {
						var dValue = $filter('filter')($scope.details, {defaultvalue:1})[0];
						$scope.selection = { value: dValue.type};
						$rootScope.type = dValue.type;
					}else {
						$scope.selection = { value: 'github'};
						$rootScope.type="github";
					} 
					
					if($scope.loggedin) {
                        $scope.details = $filter('filter')($scope.details, {type:$rootScope.type});
					} 
					
					$scope.drawSCMConfig($scope.details.length);

				}else{

					$location.path('/login');

				}


			},
			function(error) {
				console.log("error");
			});

			$scope.drawSCMConfig = function(totalScmconfig){
				$scope.rowModel = {				
						addRowCount: totalScmconfig 
				};
				console.log("totalScmconfig:"+totalScmconfig);
				if(totalScmconfig > 0){
					var row = { 
							oauthtoken: $scope.details[0].oauthtoken,
							url :$scope.details[0].url,
							id:$scope.details[0].id,
							setDefault:$scope.details[0].defaultvalue
					};

					$scope.rows = [row];
					var i;	
					for (var i = 1; i < $scope.rowModel.addRowCount; i++) {
						var row = {   
								oauthtoken: $scope.details[i].oauthtoken,
								url :$scope.details[i].url,
								setDefault:$scope.details[i].defaultvalue,
								id:$scope.details[i].id,
						};	
						$scope.rows.push(row);
					}	

				}else{
					var row = { 
							oauthtoken: '',
							url:'',
							setDefault:'0',

					};
					$scope.rows = [row];
					for (var i = 0; i < $scope.rowModel.addRowCount; i++) {		
						var row = {   
								oauthtoken: '',
								url:'',
								setDefault:'0',
						};

						$scope.rows.push(row);
					}
				}
			}

			$scope.addRows = function(currentIndex) {
				$scope.rowModel = {				
						addRowCount: 1 
				};
				for (var i = 0; i < $scope.rowModel.addRowCount; i++) {		
					var row = {   
							oauthtoken: '',
							url:'',
							setDefault:'0'
					};

					$scope.rows.push(row);
				}
			};

			$scope.removeRow = function(index,r) {								
				$scope.rows.splice(index, 1);
				console.log("remove row:"+index +"row:"+r.id);
				if(r.id!=null){
					console.log("not null");
					HomeService.removeRecord(r.id,$rootScope.scmUser,function(response) {
						$scope.status=response.data;
						if($scope.status != 'invalid'){
							$scope.status=response.data;
						}else{
							$location.path('/login');

						}

					});

				}
			};

			$scope.newValue = function(selectedVal) {
				var arrLength;
				$scope.msgSuccess=false; 
				if(selectedVal == 'github') {
					$rootScope.type=selectedVal;
					$scope.$evalAsync(function () {
						$scope.drawSCMConfig($scope.details.length);
						var data = $.param({
							userId:$rootScope.scmUser,
							type:$rootScope.type
						});
						var config = {
								headers : {
									'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
								}
						}
						$http.post('ScmUserDetailsController',data,config)
						.then(function (response) { $scope.details=response.data;
						$scope.drawSCMConfig($scope.details.length);
						},
						function(error) {
							console.log("error");
						});

					});
				}else if(selectedVal == "bitbucket"){
					$scope.details.length=0;
					$scope.drawSCMConfig($scope.details.length);
					$rootScope.type=selectedVal;
					var data = $.param({
						userId:$rootScope.scmUser,
						type:$rootScope.type
					});
					var config = {
							headers : {
								'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
							}
					}
					$http.post('ScmUserDetailsController',data,config)
					.then(function (response) { $scope.details=response.data;
					console.log("$scope.details.length:"+$scope.details.length);
					$scope.drawSCMConfig($scope.details.length);
					},
					function(error) {
						console.log("error");
					});
				}else if(selectedVal == "gitlab"){
					$scope.details.length=0;
					$scope.drawSCMConfig($scope.details.length);
					$rootScope.type=selectedVal;
					var data = $.param({
						userId:$rootScope.scmUser,
						type:$rootScope.type
					});
					var config = {
							headers : {
								'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
							}
					}
					$http.post('ScmUserDetailsController',data,config)
					.then(function (response) { $scope.details=response.data;
					console.log("$scope.details.length:"+$scope.details.length);
					$scope.drawSCMConfig($scope.details.length);
					},
					function(error) {
						console.log("error");
					});
				}
			}

			$scope.save = function(index) {
				$scope.scmSaveprogress = "load";
				$scope.saved=false;
				$scope.num=0;
				$scope.num = index;
				$scope.msgSuccess=false;
				console.log("$scope.rows[index].setDefault:"+$scope.rows[index].setDefault +"id :"+$scope.rows[index].id+"num:"+$scope.num);

				var data = $.param({
					"userId":$rootScope.scmUser,
					"switchval":$scope.rows[index].setDefault,
					"type":$rootScope.type,
					"url":$scope.rows[index].url,
					"oauthtoken":$scope.rows[index].oauthtoken, 
					"id":$scope.rows[index].id
				});
				var config = {
						headers : {
							'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
						}
				}
				$http.post('ScmPersistController',data,config)
				.then(function (response) { 
					$scope.saved=response.data;
					console.log("  $scope.saved :"+  $scope.saved);
					if($scope.saved !='invalid'){
						$scope.scmSaveprogress = "hide";
						var data = $.param({
							userId:$rootScope.scmUser,
							type:$rootScope.type
						});
						var config = {
								headers : {
									'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
								}
						}
						$http.post('ScmUserDetailsController',data,config)
						.then(function (response) { 
							$scope.details=response.data;
							console.log("$scope.details==>"+$scope.details);
							$scope.drawSCMConfig($scope.details.length);
						},
						function(error) {
							console.log("error");
						});
					}else{
						$location.path('/login');
					}//invalid


				});
			};


			$scope.testConnection = function(index) {
				$scope.scmTestprogress = "load";
				$scope.num=0;
				$scope.uname=$scope.rows[index].oauthtoken;
				$scope.uurl=$scope.rows[index].url;
				$scope.num = index;
				console.log($scope.num );
				$scope.msgSuccess=false;
				HomeService.getTestCon($scope.uname,$scope.uurl,  function(response) {

					$scope.msgSuccess=response.data;
					if($scope.msgSuccess!='invalid'){
						console.log(index +  $scope.msgSuccess);
						$scope.scmTestprogress = "hide";
					}else{

						$location.path('/login');

					}
				});
			}

			$scope.changeSwitchmode = function(switchNo ){
				var switchId;
				for (var i = 0; i < $scope.rows.length; i++) {
					if(i != switchNo) {					
						$scope.rows[i].setDefault = '0';		
					}
				}
			}
		}]);
