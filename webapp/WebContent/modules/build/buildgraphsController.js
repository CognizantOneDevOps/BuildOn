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

angular.module('Build')

.controller('buildgraphsController',
		['$scope','$location', '$mdDialog', 'dataToPass','BuildService','$rootScope' ,'graphval',
		 function ($scope,$location, $mdDialog, dataToPass,BuildService,$rootScope,graphval) {

			$scope.dataToPass = dataToPass;
			$rootScope.graphs = graphval;
			
			var daysArray = [];
			var successValArray = [];
			var failureValArray = [];
			var abortedValArray = [];
			var buildArray = [];
			var ValArray = [];
			var projectsArray = [];
			var user_successValArray = [];
			var user_successValArray = [];
			var user_failureValArray = [];
			var user_abortedValArray = [];							
			var others_successValArray = [];
			var others_failureValArray = [];
			var others_abortedValArray = [];

			if($scope.dataToPass == "buildTrends")			//BUILD TRENDS 
			{
				$scope.graph_title = "Weekly BuildTrend";
				
				$scope.buildtrendJson=$rootScope.graphs.trendsarray;

				$scope.$evalAsync(function () {
				
					var bctx = document.getElementById("chartdiv").getContext("2d");					
					
					$scope.buildtrendJson.forEach(function(data)
					{		
						daysArray.push(data.Days);
						successValArray.push(data.Success);
						failureValArray.push(data.Failure);
						abortedValArray.push(data.Aborted);
					});
				
					var barChartData = {
						labels: daysArray,
						datasets: [{
							label: 'Success',
							backgroundColor: '#84b761',
							data: successValArray
						}, {
							label: 'Failure',
							backgroundColor: '#f73333',
							data: failureValArray
						}, {
							label: 'Aborted',
							backgroundColor: '#fdd400',
							data: abortedValArray
						}]
					};
					
					window.myBar = new Chart(bctx, {
						type: 'bar',
						data: barChartData,
						options: {
							maintainAspectRatio: false,
							title:{
								display:true,
								text:"Weekly BuildTrend"
							},
							tooltips: {
								mode: 'index',
								intersect: false
							},
							responsive: true,
							scales: {
								xAxes: [{
									stacked: true,
								}],
								yAxes: [{
									stacked: true
								}]
							}
						}

					});					

				});
			}
			else if($scope.dataToPass == "lastBuild"){			//LAST BUILD

				$scope.graph_title = "Today's BuildTrend";				
				$scope.lastbuildJson = $rootScope.graphs.latestbuildarray;

				$scope.$evalAsync(function () {
				
					var lbtctx = document.getElementById("chartdiv").getContext("2d");
						
					$scope.lastbuildJson.forEach(function(data)
					{		
						buildArray.push(data.build_status);
						ValArray.push(data.value);							
					});
					
					var config = {
						type: 'pie',
						data: {
							datasets: [{
								data: ValArray,
								backgroundColor: [
								      '#f73333',            
									  '#84b761',									 
									  '#fdd400'
									  ],
									label: 'Todays BuildTrend'
							}],
							labels: buildArray
						},
						options: {
							maintainAspectRatio: false,
							title:{
								display:true,
								text:"Today's BuildTrend"
							},
							tooltips: {
								mode: 'index',
								intersect: false
							},
							responsive: true
						}
					};
					window.myPie = new Chart(lbtctx, config);	
					
				});

			}
			else if($scope.dataToPass == "projectBuilds")			//PROJECT BUILD 
			{		
				$scope.graph_title = "Project wise BuildTrend";
				
				$scope.projectbuildJson = $rootScope.graphs.projwisearray;

				$scope.$evalAsync(function () {

					var pbctx = document.getElementById("chartdiv").getContext("2d");
					$scope.projectbuildJson.forEach(function(data)
					{		
						projectsArray.push(data.Projects);
						successValArray.push(data.Success);
						failureValArray.push(data.Failure);
						abortedValArray.push(data.Aborted);
					});						
					
					var barChartData = {
							labels: projectsArray,
							datasets: [{
								label: 'Success',
								backgroundColor: '#84b761',
								data: successValArray
							}, {
								label: 'Failure',
								backgroundColor: '#f73333',
								data: failureValArray
							}, {
								label: 'Aborted',
								backgroundColor: '#fdd400',
								data: abortedValArray
							}]

					};
					
					window.myBar = new Chart(pbctx, {
							type: 'bar',
							data: barChartData,
							options: {
								maintainAspectRatio: false,
								title:{
									display:true,
									text:"Project wise BuildTrend"
								},
								tooltips: {
									mode: 'index',
									intersect: false
								},
								responsive: true,
								scales: {
									xAxes: [{
										stacked: true,
									}],
									yAxes: [{
										stacked: true
									}]
								}
							}

					});

				});
			}
			else if($scope.dataToPass == "comparisonBuilds") 			//COMPARE BUILDS
			{				
				$scope.graph_title = "You vs Others";
				
				$scope.comparisonbuildJson = $rootScope.graphs.comparebuildarray;

				$scope.$evalAsync(function () {
				
					var cmpctx = document.getElementById("chartdiv").getContext("2d");
					
						$scope.comparisonbuildJson.forEach(function(data)
						{		
							projectsArray.push(data.projects);
							user_successValArray.push(data.you_success);
							user_failureValArray.push(data.you_failure);
							user_abortedValArray.push(data.you_aborted);							
							others_successValArray.push(data.others_success);
							others_failureValArray.push(data.others_failure);
							others_abortedValArray.push(data.others_aborted);
							
						});
						
						var barChartData = {
						labels: projectsArray,
						datasets: [{
								label: 'Success',
								backgroundColor: '#84b761',
								stack: 'Stack 0',
								data: user_successValArray
							}, {
								label: 'Failure',
								backgroundColor: '#f73333',
								stack: 'Stack 0',
								data: user_failureValArray
							}, {
								label: 'Aborted',
								backgroundColor: '#fdd400',
								stack: 'Stack 0',
								data: user_abortedValArray
							},{
								label: 'Others Success',
								backgroundColor: '#cc4748',
								stack: 'Stack 1',
								data: others_successValArray
							}, {
								label: 'Others Failure',
								backgroundColor: '#cd82ad',
								stack: 'Stack 1',
								data: others_failureValArray
							}, {
								label: 'Others Aborted',
								backgroundColor: '#2f4074',
								stack: 'Stack 1',
								data: others_abortedValArray
							}]
						};
				
						window.myBar = new Chart(cmpctx, {
							type: 'bar',
							data: barChartData,
							options: {
								maintainAspectRatio: false,
								title:{
									display:true,
									text:"You vs Others"
								},
								tooltips: {
									mode: 'index',
									intersect: false
								},
								responsive: true,
								scales: {
									xAxes: [{
										stacked: true,
									}],
									yAxes: [{
										stacked: true
									}]
								}
							}
						});
					
				});
			}
			
			function addLegendLabel(e) {
				  var title = document.createElement("div");
				  /*title.innerHTML = "Build Status";*/
				  title.className = "legend-title";
				  e.chart.legendDiv.appendChild(title)
				}


			$scope.cancel = function() {
				$mdDialog.cancel();
			};

		}]);
