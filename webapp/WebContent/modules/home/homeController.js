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



.controller('HomeController',
		['$scope','$timeout', '$location', '$rootScope','$route', 
		 function ($scope,$timeout, $location, $rootScope,$route) {

			$scope.imageurl1 = "dist/icons/svg/landingPage/HCM_normal.svg";
			$scope.imageurl2 = "dist/icons/svg/landingPage/Build_on_normal.svg";
			$scope.imageurl3 = "dist/icons/svg/landingPage/Preference_normal.svg";
			$scope.imageurl4 = "dist/icons/svg/landingPage/Individual_build_report_normal.svg";
			$scope.imageurl5 = "dist/icons/svg/landingPage/Historical_reports_normal.svg";



			$scope.footerMinHeight = 'min-height:' + (window.innerHeight - 146) + 'px;';
			$scope.footerHeight = '';
			$scope.mainContentMinHeight = 'min-height:' + (window.innerHeight - 146 - 96) + 'px';
			/*$scope.mainContentMinHeight = 'min-height: 468px';*/
			$scope.mainContentMinHeightWoSbTab = 'min-height:' + (window.innerHeight - 146 - 48) + 'px';	
			
			$scope.mainOuterheight = 'min-height:' + (window.outerHeight) + 'px';

			$scope.addSelectedImage = function(selectedTab) {

				if (selectedTab == 'scm') {
					this.imageurl1 = "dist/icons/svg/landingPage/HCM_selected.svg";
				}
				else if (selectedTab == 'buildon') {
					this.imageurl2 = "dist/icons/svg/landingPage/Build_on_selected.svg";
				}
				else if (selectedTab == 'preference') {
					this.imageurl3 = "dist/icons/svg/landingPage/Preference_selected.svg";
				}
				else if (selectedTab == 'ind_build_report') {
					this.imageurl4 = "dist/icons/svg/landingPage/Individual_build_report_selected.svg";
				}
				else if (selectedTab == 'historical_report') {
					this.imageurl5 = "dist/icons/svg/landingPage/Historical_reports_selected.svg";
				}
			}


			$scope.removeSelectedImage = function(selectedTab) {

				if (selectedTab == 'scm') {
					this.imageurl1 = "dist/icons/svg/landingPage/HCM_normal.svg";
				}
				else if (selectedTab == 'buildon') {
					this.imageurl2 = "dist/icons/svg/landingPage/Build_on_normal.svg";
				}
				else if (selectedTab == 'preference') {
					this.imageurl3 = "dist/icons/svg/landingPage/Preference_normal.svg";
				}
				else if (selectedTab == 'ind_build_report') {
					this.imageurl4 = "dist/icons/svg/landingPage/Individual_build_report_normal.svg";
				}
				else if (selectedTab == 'historical_report') {
					this.imageurl5 = "dist/icons/svg/landingPage/Historical_reports_normal.svg";
				}            
			}

			$scope.goTopath = function(goData) {
				$timeout(function () { 
					$location.path(goData);
				});
			}

			$scope.logout = function() {
				$scope.templateName = '';
				$location.path('/login');
				$route.reload();
				
				
			}
			
			if($scope.templateName == "" || $scope.templateName === undefined) {		
				$scope.templateName = 'scm';
			}

			$scope.changeTab = function(tabName,tabNum) {

				$scope.$evalAsync(function () {
					$scope.templateName = tabName;
					$scope.selectedTab = tabNum;
				});
				
				if(tabName == "ind_build_report") {
					
					$rootScope.showIndividualreport = true;
				}else {
					$rootScope.showIndividualreport = false;
				} 

			}



			$scope.labels = ['1', '2', '3', '4', '5'];
			$scope.series = ['Latest build status'];
			$scope.data = [ [20,20,30,40, 50],
			                [28, 48, 40, 19, 86, 27, 90]
			];

			$scope.labels1 = ['1', '2', '3', '4', '5'];
			$scope.series1 = ['Project wise'];
			$scope.data1 = [ [20,20,30,40, 50], [28, 48, 40, 19, 86, 27, 90]
			];



			$scope.project = [
			                  {value: 1, name: "Buildon"},
			                  {value: 2, name: "Bitbucket"},
			                  {value: 2, name: "LogAccelerator"}
			                  ];
			$scope.enrollments =[
			                     {name: "Your project", id: 1, rating: 1},
			                     {name: "your rating", id: 3, rating: 1},
			                     {name: "Choose image", id:  5, rating: 1}
			                     ];
			
			$rootScope.showIndividualreport = false; 



		}]);
