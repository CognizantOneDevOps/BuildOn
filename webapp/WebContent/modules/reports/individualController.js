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
.config(function($httpProvider) {
	$httpProvider.useApplyAsync(true);
})
.controller('individualController',
		['$scope', '$location', '$interval','$http', '$rootScope', '$timeout','IndividualService','$q',
		 function ($scope,$location,$interval,$http, $rootScope, $timeout, IndividualService,$q) {
			$scope.pipelineConfig=pipeUrl;
			$scope.kubelogConfig=kubeUrl;
			$scope.cilogConfig =cilogurl;
			$scope.abortUrl =abort;
			$scope.dbservice=dbserviceurl;

			var ciNewlogs = "";
			var ciAppendlogs = "";			
			var fetchUser = "";


			if($rootScope.shwfromhist){
				fetchUser = $rootScope.histuser;
				console.log($rootScope.histuser);
				console.log($rootScope.histcommitid);
				IndividualService.getIndividualStatusReports($rootScope.histuser,$rootScope.histcommitid,function(response) {
					if(response.data != 'invalid'){
					
					if(response.data != "") {
						$scope.details=response.data;
					}else {
						$scope.details.CommitID = '0';			    			
					}
					$timeout(function () {
						$rootScope.CommitId = $scope.details.CommitID;
						$rootScope.repo = $scope.details.Project;
						$rootScope.branch = $scope.details.Branch;
						$rootScope.jobid = $scope.details.JobId;
						$rootScope.jobStatus = $scope.details.status;
						$scope.cilogUrl =$rootScope.CommitId;

						//Infra and CIlog file url
						$scope.individualTab("pipeline");						

					},500);
					
					}else{
						$location.path('/login');
					}
				});
			}

			if(!$rootScope.shwfromhist){
					fetchUser = $rootScope.scmUser;
					console.log($rootScope.scmUser);
					IndividualService.getIndividualReports($rootScope.scmUser,function(response) {
						if(response.data != 'invalid'){
						
						
						if(response.data != "") {
							$scope.details=response.data;
						}else {
							$scope.details.CommitID = '0';			    			
						}
						console.log( $scope.details);
						$timeout(function () {
							console.log( $scope.details);
							console.log("hist controller $scope.details.CommitID :"+$scope.details.CommitID);
							$rootScope.CommitId = $scope.details.CommitID;
							$rootScope.repo = $scope.details.Project;
							$rootScope.branch = $scope.details.Branch;
							$rootScope.jobid = $scope.details.JobId;
							$rootScope.jobStatus = $scope.details.status;
							console.log($rootScope.jobStatus);

							$rootScope.infralogFilepath = $scope.details.status;
							$rootScope.cilogFilepath = $scope.details.status;

							//$scope.hit = "hitinitiated";
							
							
							console.log("commit id hist controller......"+$rootScope.CommitId );
							IndividualService.getDBServiceUpdate($rootScope.CommitId,$rootScope.repo,$rootScope.branch,function(response){
								
								
								if(response.data != 'invalid'){
									$scope.update=response.data;
									console.log("DBSERVICE UPDATE..."+$scope.update);	
								}else{
									$location.path('/login');
									
								}
								
							});	
							
							$scope.cilogUrl = $rootScope.CommitId;
							$scope.individualTab("pipeline");

						},500);
						
						}else{
							
							$location.path('/login');
							
						}
						

					});

				
			}

			$scope.piplelineShow = false;			
			$scope.infralogShow = false;
			$scope.cilogShow = false;
			$scope.infraLogDataEx = "";
			var ciAppendlogs = "";
			var tabName;

			$scope.individualreportFeed = function(hitType) {

				if(hitType == "pipeline"){

					if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" &&  $rootScope.jobStatus != "Failure" && $rootScope.jobStatus != "Aborted")) {							

						IndividualService.getJsonData($rootScope.CommitId,function(response) {													
							$scope.pipelineData =response.data;
							if($scope.pipelineData != 'invalid'){
							IndividualService.getIndividualStatusReports(fetchUser,$rootScope.CommitId,function(response) {
								$scope.details=response.data;
								
								if(response.data != 'invalid'){
								$rootScope.jobStatus = $scope.details.status;
								console.log($scope.details);
								console.log("pipeline interval hit .."+$rootScope.jobStatus);
								}else{
									
									$location.path('/login');
									
								}
							});	
							
							$scope.individualreportFeed('pipeline');
							
						}else{
							$location.path('/login');
							
						}
						});

					} else if($scope.templateName == "ind_build_report" && $rootScope.shwfromhist) {

						IndividualService.getJsonData($rootScope.CommitId,function(response) {
							$scope.pipelineData =response.data;
							if($scope.pipelineData != 'invalid'){
								$scope.pipelineData =response.data;
								
							}else{
								
								$location.path('/login');
							}
							
						});

					}

				}
				else if(hitType == "infralogs"){

					$scope.infralogShow = true;

					if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" && $rootScope.jobStatus != "Failure" && $rootScope.jobStatus != "Aborted")) 
					{
						// using XMLHttpRequest
						var xhr =new XMLHttpRequest();


						//xhr.open("GET",$scope.kubelogConfig+"/kubelog?data="+$rootScope.CommitId,true);
						xhr.open("GET","KubernetesWebController?commitId="+$rootScope.CommitId,true);

						xhr.onload = function(){

							$scope.infralogShow = false;

							if (xhr.readyState === 4) {

								if (xhr.status === 200) {	

									$scope.infraLogDataEx = xhr.responseText;
									$("#infraLogData").html($scope.infraLogDataEx);

								} else if(xhr.status === 500){
									$("#infraLogData").html("<p>Error in Response..please wait..</p>");
								}
							}

							$scope.individualreportFeed('infralogs'); 
							
							var Infra = $('#infraLogData');
							Infra.scrollTop(Infra.prop("scrollHeight"));


						};
						xhr.onerror = function (e) {

							$scope.infralogShow = false;
							$("#infraLogData").html("<p>Error on Request, please wait....</p>")
							if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" && $rootScope.jobStatus != "Failure" && $rootScope.jobStatus != "Aborted")) {
								$scope.individualreportFeed('infralogs'); 
							}
						};
						xhr.send();			
					}
					else if($scope.templateName == "ind_build_report" && $rootScope.shwfromhist) {
						$scope.historicKube="HistoricWebController?commitId="+$rootScope.CommitId;
						$http.get($scope.historicKube)
						.then(function (data) {		  
							$scope.infralogShow = false;							
							$("#infraLogData").html(data.data);
						}, function (error) {		         
							$scope.infralogShow = false;						
							$("#infraLogData").html("Error to read infra log file.");
						});

					}
					else {
						
						 $timeout(function () {
						 
							if($scope.infraLogDataEx) {
									$("#infraLogData").html($scope.infraLogDataEx);						
							}
							else{
									$("#infraLogData").html("<p>Job has been Completed. To view logs, go to <b><i>Historical page</i></b> </p>");
							 }
									$scope.infralogShow = false;							 
								
						 },100);
					}


				}else if(hitType == "cilogs"){

					$scope.cilogShow = true;

					if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" && $rootScope.jobStatus != "Failure" && $rootScope.jobStatus != "Aborted")) 
					{

						// using XMLHttpRequest
						var xhr =new XMLHttpRequest();

						xhr.open("GET","CILogWebController?commitId="+$rootScope.CommitId,true);
						xhr.onload = function(){

							$scope.cilogShow = false;
							if (xhr.readyState === 4) {

								if (xhr.status === 200) {

									if(xhr.responseText == "next_job" && (ciNewlogs == "" || ciNewlogs == "next_job")){									

										if(ciAppendlogs) {
											$("#ciLogData").html(ciAppendlogs);
										}else {		
											console.log(ciAppendlogs);								
											$("#ciLogData").html("<p>Job loading..Please wait...</p>");
										}

									}
									else if(xhr.responseText == "next_job" && ciNewlogs != "next_job") {
										console.log("sfasdfasd");
										ciAppendlogs = ciAppendlogs+ciNewlogs;
										$("#ciLogData").html(ciAppendlogs);

									}
									else {
										if(ciAppendlogs) {
											$("#ciLogData").html(ciAppendlogs+xhr.responseText);
										} else {
											$("#ciLogData").html(xhr.responseText);
										}

									}

									ciNewlogs = xhr.responseText;
									
									var CiL = $('#ciLogData');
									CiL.scrollTop(CiL.prop("scrollHeight"));


								}else if(xhr.status === 500) {

									$("#ciLogData").html("<p>Error in Response..please wait..</p>");
								}
							}
							
							$scope.individualreportFeed('cilogs');
						};
						xhr.onerror = function (e) {

							$scope.cilogShow = false;                    
							$("#ciLogData").html("<p>Error on Request, please wait....</p>")
							if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" && $rootScope.jobStatus != "Failure" && $rootScope.jobStatus != "Aborted")) {
								$scope.individualreportFeed('cilogs');
							}

						};

						xhr.send();
						//if($scope.templateName == "ind_build_report" && ($rootScope.jobStatus != "Success" || $rootScope.jobStatus != "Failure" || $rootScope.jobStatus != "Aborted")) {
						//$scope.individualreportFeed('cilogs');
					}
					else if($scope.templateName == "ind_build_report" && $rootScope.shwfromhist) {

						//$http.get($rootScope.infralogFilepath) //when I try to read cities.json error occurs
						
						$scope.historicCI="HistoricCIWebController?commitId="+$rootScope.CommitId;
						$http.get($scope.historicCI)
						.then(function (data) {		      
							$scope.cilogShow = false;      						
							$("#ciLogData").html(data.data);
						}, function (error) {	
							$scope.cilogShow = false;      
							$("#ciLogData").html("Error to read cilog log file.");
						});

					}	
					else { 
					  $timeout(function () {
					  
							if(ciAppendlogs) {
								$("#ciLogData").html(ciAppendlogs);
							}
							else {
								$("#ciLogData").html("<p>Job has been Completed. To view logs, go to <b><i>Historical report</i></b> page.</p>");
							 }							 
							$scope.cilogShow = false;                           
                     				 },100);

					}

				}

			}

				$scope.individualTab = function(tabName) {

					$scope.activeTabname = "";
					$scope.activeTabname = tabName;		

					if($rootScope.CommitId != '0') {

						$scope.individualreportFeed(tabName);

					}

				}	

				$scope.abortProcess = function() {
					$scope.aborthit=$scope.abortUrl+"/abort?data="+$rootScope.jobid;
					$http.get($scope.aborthit).then(successCallback);
					function successCallback(response){
						console.log("success"+response);
					}
				}


				$scope.getJsonData = function() {
					console.log("$rootScope.CommitId.."+$rootScope.CommitId);
					IndividualService.getJsonData($rootScope.scmUser,$rootScope.CommitId,function(response) {
						
						
						if(response.data != 'invalid'){
							$rootScope.jsonData=response.data;
						console.log("function"+$rootScope.jsonData);
						}else{
							
							$location.path('/login');
						}
					});
				}

			}]);
