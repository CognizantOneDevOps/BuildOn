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

angular.module('Build', ['ui.ace'])

.controller('BuildController',
		['$scope','$location', '$mdDialog','BuildService','$rootScope','$timeout','$interval','$http', 
		 function ($scope,$location, $mdDialog,BuildService,$rootScope,$timeout,$interval,$http) {
			$rootScope.shwfromhist=false;
			$scope.formData = {}; 
			$rootScope.buildtrendJson = {};
				$rootScope.$broadcast('userid',$rootScope.scmUser);
				console.log($rootScope.scmUser + $rootScope.type);
				BuildService.getRepoDetails($rootScope.scmUser,$rootScope.type,function(response) {
					console.log($rootScope.userId + $rootScope.scmUser); 
					$rootScope.obj = response.data;
					console.log($scope.obj);
					
					if($rootScope.obj != 'invalid'){
					angular.forEach( $rootScope.obj, function(value, key) {					
						$scope.items=$scope.obj.repoList;
						$rootScope.buildtrendJson =$scope.obj.trendsarray;	
						$rootScope.projectbuildJson = $scope.obj.projwisearray;	
						$rootScope.lastbuildJson = $scope.obj.latestbuildarray;	
						$rootScope.comparisonbuildJson = $scope.obj.comparebuildarray;

					});
					$rootScope.$broadcast('graphdatas',$scope.obj);
					$timeout(function () {
						BuildService.drawGraph($rootScope.buildtrendJson, "buildTrendsDiv", "buildTrends");
					}, 500);

					$timeout(function () {
						BuildService.drawGraph($rootScope.lastbuildJson, "lastbuildDiv", "lastBuild");
					}, 500); 
					$timeout(function () {
						BuildService.drawGraph($rootScope.projectbuildJson, "projectbuildsDiv", "projectBuilds");
					}, 500);

					$timeout(function () {
						BuildService.drawGraph($rootScope.comparisonbuildJson, "comparebuildsDiv", "comparisonBuilds");
					}, 500);


					}else{
						$location.path('/login');
					}
					
					
					
				});

		//	});
			
			$rootScope.$on('userid', function (event, args) {
				$rootScope.userId  = args;
			});

			$rootScope.$on('graphdatas', function (event, args) {
				$scope.graphval = args;
			});


			$scope.customFullscreen = true;

			$scope.logout = function () {
				$location.path('/login');
			}

			$scope.submit = function () {
				$location.path('/home');

			}
			$scope.btndisable=true;
			$scope.select = function (project,source) {
				
				if(source =='bran'){
					$rootScope.selectedbranch=project;
					$scope.isLoadingbranch = true;
					$scope.isLoadingtxt=true;
					BuildService.getJenkinsFile($rootScope.scmUser,$rootScope.selectedbranch,$rootScope.selectedproj,$rootScope.type,function(response) {
						$scope.yamlContent=response.data;
						//console.log("$scope.yamlContent:"+$scope.yamlContent);
						$scope.show = false;
						
						if($scope.yamlContent !='invalid'){
						
						if($scope.yamlContent=="No Jenkinsfile"){
							$scope.btndisable =true;
							$scope.yaml="Jenkinsfile not found"
							$scope.isLoadingtxt=false;
						}else{
							$scope.btndisable =false;
							$scope.yaml="Jenkinsfile";
							$scope.isLoadingbranch =false;
							$scope.isLoadingtxt=false;
						}
						}else{
							
							
							$location.path('/login');
						}
				});

				}else if(source =='proj'){
					$rootScope.selectedproj=project;
					$scope.isLoading = true;
					console.log($rootScope.scmUser);
					BuildService.getScmDetails($rootScope.scmUser,$rootScope.selectedproj,$rootScope.type, function(response) {
						$scope.branches=response.data;
						console.log($scope.branches);
						if($scope.branches != 'invalid'){
						
						$scope.isLoading = false;
						}else{
							
							$location.path('/login');
							
							
						}

					});

				}		     
			}
			

			$scope.showAdvanced = function(ev) {
				$scope.yaml="Jenkinsfile";
				$mdDialog.show({
					controller: DialogController,
					templateUrl: './modules/build/views/edit.tmp.html',							
					parent: angular.element(document.body),
					targetEvent: ev,  
					clickOutsideToClose:true,					
					bindToController: true,
					fullscreen: $scope.customFullscreen // Only for -xs, -sm breakpoints.
				})
				.then(function(answer) {
					$scope.status = 'You said the information was "' + answer + '".';
				}, function() {
					$scope.status = 'You cancelled the dialog.';
				});
			};

			function DialogController($scope, $mdDialog) {
				$scope.show = true;
				
				
				
				BuildService.getJenkinsFile($rootScope.scmUser,$rootScope.selectedbranch,$rootScope.selectedproj,$rootScope.type, function(response) {
					$scope.yamlContent=response.data;
					console.log( "get jenkins:" + $scope.yamlContent);
					
					if($scope.yamlContent !='invalid'){
						$scope.show = false;
					}else{
						
						$location.path('/login');
						
					}
					
					
						 
				});

				$scope.aceOption = {
						theme: 'chrome',
						require: ['ace/ext/spellcheck'],
						require: ['ace/ext/language_tools'],
						advanced: {
							enableSnippets: true,
							enableBasicAutocompletion: true,
							enableLiveAutocompletion: true,
							spellcheck:true,
							useWorker:true
						},
						rendererOptions: {
							maxLinks: Infinity
						},
						useWrapMode : true,					
						mode: 'yaml',
						onLoad: function (_ace) {
							$scope.isDisabled=true;
							$scope.isDisabledSave=true;
							var _session = _ace.getSession();
							var _renderer = _ace.renderer;  
							// HACK to have the ace instance in the scope...
							$scope.modeChanged = function () {
								_ace.getSession().setMode("ace/mode/yaml");

							};
							// Options 
							_session.setUndoManager(new ace.UndoManager());
							_session.setOption("useWorker", true);
							_renderer.setShowGutter(true);
							_ace.setFontSize(16);						
							_ace.setHighlightActiveLine(true);
							_ace.on("changeSession", function(){
							});

							$scope.val=0;

						},
						onChange: function() {
							if($scope.val==0){
								$scope.isDisabledSave=true;
							}
							if($scope.val>0){
								$scope.isDisabledSave=false;
							}
							$scope.val=1;
						}
				};
				$scope.cancel = function() {
					$mdDialog.cancel();
				};
				$scope.save = function () {
					$rootScope.reportshw=true;
					$scope.isDisabled=false;
					$scope.imgshow=true;
					$scope.content=$scope.yamlContent;
					var data = $.param({
						userId: $rootScope.scmUser,
						content: $scope.content,
						type:$rootScope.type
		            });
		            var config = {
		                headers : {
		                    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
		                }
		            }
		            $http.post('JenkinsWebController',data,config)
		           .then(function (response) {
		        	   $scope.projdata=response.data;
		        	   
		        	   if( $scope.projdata!='invalid'){
						$scope.imgshow=false;
						$scope.cancel();
						console.log( $scope.projdata);
		        	   }else{
		        		   $location.path('/login');
		        		   
		        	   }
		            }, function(error) {
					console.log("error");
					});
				}
				$scope.changeTab = function(tabName,tabNum) {
					console.log(tabName);
					$timeout( function(){
						$scope.templateName = tabName;
						$scope.selectedTab = tabNum;
					}, 500);

				}

				$scope.buildon = function () {
					$rootScope.reportshw=true;
					$scope.imgshowbuild=true;
					$scope.content=$scope.yamlContent;
					BuildService.buildon($rootScope.scmUser,$rootScope.selectedbranch,$rootScope.selectedproj,$scope.content,function(response) {
						$scope.projdata=response.data;
						console.log($scope.projdata);
						$scope.imgshowbuild=false;
						$scope.cancel();
					});

				}
			}

			$scope.showGraph = function(ev, graphType) {
				$scope.gTypes = graphType;
				$mdDialog.show({
					controller: 'buildgraphsController',
					templateUrl: './modules/build/views/lastbuildStatus.tmp.html',
					parent: angular.element(document.body),
					targetEvent: ev,  
					locals:{dataToPass: $scope.gTypes ,graphval: $scope.graphval}, 
					clickOutsideToClose:true,					
					bindToController: true,
					fullscreen: $scope.customFullscreen // Only for -xs, -sm breakpoints.
				});
			}
			$scope.callBuildon = function() {
				$rootScope.reportshw=true;
				$scope.loadtab=true;
				console.log($rootScope.scmUser+$rootScope.selectedproj+$rootScope.selectedbranch+$rootScope.userId + $rootScope.type);
				 BuildService.callBuildon($rootScope.scmUser,$rootScope.selectedbranch,$rootScope.selectedproj,$rootScope.type).then(function (response) {
						console.log(response.data);
						$scope.status=response.data;
						if($scope.status != "invalid"){
						$rootScope.showIndividualreport = true;
						$scope.loadtab=false;
						$rootScope.CommitId ='0';
						$scope.details={};
						$scope.changeTab('ind_build_report','3');
						}else{
							
							$location.path('/login');
						}
					}, function(error) {
					console.log("error");
					}); 
			}
			
			
			 $interval.cancel($rootScope.promise);
		}]);
