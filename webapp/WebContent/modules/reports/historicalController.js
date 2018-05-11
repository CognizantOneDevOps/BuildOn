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

angular.module('Historical')
 
.controller('historicalController',
    ['$scope', '$location', 'NgTableParams','HistoricalService','$rootScope','$mdDateLocale','$filter','$timeout','$interval',
    function ($scope,$location, NgTableParams,HistoricalService,$rootScope,$mdDateLocale,$filter,$timeout,$interval) {
    	
    	$scope.startDate = new Date();
    	$scope.endDate = new Date();
	$scope.today = new Date();

    	$scope.selection = { value: 'self'};
    	$rootScope.intiatedBy='self';
    	console.log($rootScope.scmUser);
    	HistoricalService.getHistoricalReports($rootScope.scmUser,function(response) {
			  $scope.details=response.data;
		//	  $scope.branches=  $scope.details.branchlist;
			  $scope.projects=  $scope.details.projectlist;
    	});
    	
		$scope.newValue = function(selectedVal) {
			$rootScope.intiatedBy=selectedVal;	
		}

 $scope.checkError = function(startDate,endDate) {
        $scope.errMessage = '';
        var curDate = new Date();
        
        if(new Date(startDate) > new Date(endDate)){
          $scope.errMessage = '***End Date should be greater than Start date***';
          return false;
        }
    };

     	$scope.submitForm = function(sdate,edate) {
     		var srtdate=sdate;
     		var enddate=edate;
			var newstartDate = $filter('date')(srtdate, "yyyy-MM-dd");
			var newendDate = $filter('date')(enddate, "yyyy-MM-dd");
			$scope.selproj='';
			$scope.selproj=$rootScope.histselectedproject;
			if($rootScope.histselectedproject ==null ||$rootScope.histselectedproject ==' ' ||$rootScope.histselectedproject =='undefined'){
				$rootScope.histselectedproject=null;
			}			
			console.log($rootScope.histselectedproject+"..." );
			HistoricalService.getSearchresult($rootScope.scmUser,$rootScope.histselectedproject,$rootScope.intiatedBy,
				  newstartDate,newendDate,function(response) {
				  $scope.details=response.data;
				  if( $scope.details.length >0){
					  $scope.tbshow=true;
					  $scope.selproj='';
				  }else{
					  $scope.tbshow=false;
				  }
				var dataset =$scope.details
			/*	$scope.tableParams = new NgTableParams({
		 			count: 5
		 		}, {
		 			data: dataset
		 		}); */				
				
				$scope.tableParams = new NgTableParams({				
					page: 1,
					count: 5				
				}, 
				{ 
					counts: [], // hide page counts control
					total: 1,  // value less than count hide pagination				
					data: dataset
				}); 
				
				//$rootScope.histselectedproject='';
				//$rootScope.histselectedbranch='';
				
				  
	     	});
			
			
		};
		
		
		$scope.showIndreport = function(id,histscmuser) {
			console.log("histscmuser: "+histscmuser);
					$rootScope.shwfromhist=true;
					$rootScope.showIndividualreport = true;					
					$rootScope.histcommitid= id;
					$rootScope.histuser=histscmuser;
					console.log("histscmuser: "+$rootScope.histuser);
					HistoricalService.getReportTriggerData($rootScope.histcommitid,function(response) {
						  $scope.details=response.data;
						  console.log(response.data);					 
						  if($scope.details=='FRAMEWORK' || $scope.details=='BOT')
							  {
								HistoricalService.getHistoricDBService($rootScope.histcommitid,function(response) {
									  $scope.details=response.data;
									  console.log(response.data);
									  console.log($rootScope.histcommitid);							      
									  $scope.changeTab('ind_build_report','3'); 		
						     	});										  
							  }
						  else
							  {						    
								$scope.changeTab('ind_build_report','3'); 
							  }
					    
			     	});
			}
		
		 $interval.cancel($rootScope.promise);
    	
    }]);
