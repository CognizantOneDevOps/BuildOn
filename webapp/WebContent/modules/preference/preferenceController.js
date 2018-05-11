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
 angular.module('Preference')
.controller('preferenceController',
    ['$scope','$location', 'NgTableParams','PreferenceService','$rootScope','$interval',
    function ($scope, $location, NgTableParams,PreferenceService,$rootScope,$interval) {
    	
    	//$rootScope.type="github";

    	console.log($rootScope.scmUser);
    	PreferenceService.getPreferenceDetails($rootScope.scmUser,$rootScope.type, function(response) {
			  $scope.details=response.data;
			  if($scope.details != 'invalid'){
				  
				  $scope.details=response.data;
				  
			  }else{
				  $location.path('/login');				  
			  }
			  
    	});
    	
    	
		$scope.changeSwitchmode = function(switchmode,repo,index){
			PreferenceService.savePreferenceDetails($rootScope.scmUser,switchmode,repo,$rootScope.selectedbranch, function(response) {
				  $scope.det=response.data;
				  if(  $scope.det!='invalid'){
					  
					  $scope.det=response.data;
					  
				  }else{
					  $location.path('/login');		
				  }
	    	});	
		}
		
		$interval.cancel($rootScope.promise);
    	
    }]);
