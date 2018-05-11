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

angular.module('Build')

.factory('BuildService',
		['Base64', '$http', '$cookieStore', '$rootScope', '$timeout','$q',
		 function (Base64, $http, $cookieStore, $rootScope, $timeout,$q) {
			var service = {};
			
			service.getRepoDetails = function (userId,type,callback) {
				var response =$http({
					url : 'GithubRepoWebController',
					method: "GET",
					params: {
						"userId":userId ,
						"type":type 
					}
				})
				.then(function successCallback(response,status) {		
					callback(response); 
				}, function errorCallback (response,status) {
					callback(response);
				});
			
			};
			service.getScmDetails = function (userId,repo,type,callback) {
				var response =$http({
					url : 'GithubWebController',
					method: "GET",
					params: {
						"userId":userId,
						"repo":repo,
						"type":type
					}
				})
				.then(function successCallback(response,status) {		
					callback(response); 
				}, function errorCallback (response,status) {
					callback(response);
				});
			
			};
			
			service.getJenkinsFile = function (userId,branch,project,type,callback) {
					var response =$http({
						url : 'GithubWebController',
						method: "POST",
						params: {
							"userId":userId ,
							"branch":branch,
							"project":project,
							"type":type
						}
					})
					.then(function successCallback(response,status) {		
						callback(response); 
					}, function errorCallback (response,status) {
						callback(response);
					});
				
				};
				
					
				service.buildon = function (userId,branch,repo,content,callback) {
						var response =$http({
							url : 'JenkinsWebController',
							method: "GET",
							params: {
								"userId":userId,
								"content":content,
								"branch":branch,
								"repo":repo
							}
						})
						.then(function successCallback(response,status) {		
							callback(response); 
						}, function errorCallback (response,status) {
							callback(response);
						});
				};
				
				service.drawGraph = function(dataJson, graphdivId, graphType){
				
					
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
					
					if(graphType == "buildTrends") {			//BUILD TRENDS		

						var bctx = document.getElementById("buildTrendsChartjs").getContext("2d");					
					
						dataJson.forEach(function(data)
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
								legendCallback: function(Chart) {
									console.log(Chart.data.datasets[0].backgroundColor);
									 var text = [];						                
								for (var i=0; i < 3; i++) {						                
											text.push('<div class="chartlegendLabel" style="background-color:' + Chart.data.datasets[i].backgroundColor + '; border:1px solid '+Chart.data.datasets[i].backgroundColor+'">' + Chart.data.datasets[i].label + '</div>');
										}						                
										return text.join("");
								 },
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
						
						document.getElementById('chart-legends').innerHTML = myBar.generateLegend();
						
					}
					else if(graphType == "lastBuild"){			//LAST BUILD						
					
						var lbtctx = document.getElementById("lastbuildDivChartjs").getContext("2d");
						
						dataJson.forEach(function(data)
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
								animateRotate: true,
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
					}
					else if(graphType == "projectBuilds") {				//PROJECT BUILD TRENDS
						
						var pbctx = document.getElementById("projectbuildsDivChartjs").getContext("2d");
					
						if(dataJson) {
							dataJson.forEach(function(data)
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
						}
						
					}
					else if(graphType == "comparisonBuilds") 			//COMPARE BUILDS
					{					
						var cmpctx = document.getElementById("comparebuildsDivChartjs").getContext("2d");
						
						dataJson.forEach(function(data)
						{		
							projectsArray.push(data.projects);
							user_successValArray.push(data.you_success);
							user_failureValArray.push(data.you_failure);
							user_abortedValArray.push(data.you_aborted);							
							others_successValArray.push(data.others_success);
							others_failureValArray.push(data.others_failure);
							others_abortedValArray.push(data.others_aborted);
							
						});
						
						var compare_barChartData = {
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
							data: compare_barChartData,
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
						
						
					}
					


				}
				service.callBuildon = function (userId,branch,repo,type,callback) {
					var defered = $q.defer();
					$http({
							url : 'BuildonWebController',
							method: "POST",
							params: {
									"userId":userId,
									"branch":branch,
									"repo":repo,
									"type":type
									
								}
							})
							.then(function(response){
								console.log(response);
								defered.resolve(response);
							  },function(response) {
									defered.reject(response);
							 });
						
					return defered.promise; 
					
			};
			

			return service;
		}]);//End of factory
