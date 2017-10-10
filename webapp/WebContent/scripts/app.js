'use strict';

// declare modules
angular.module('Authentication',['ngMessages']);
angular.module('Build', ['ngMaterial']);
angular.module('Home', ["chart.js",'angularjs-crypto']);
angular.module('Editor', ['ui.ace']);
angular.module('Individual', ['ngStorage']);
angular.module('Historical', ['ngMaterial', 'ngTable']);
angular.module('Preference', ['ngTable']);

angular.module('Buildon', [
    'Authentication',
    'Build',
    'Home',
    'Editor',
    'Individual',
    'Historical',
    'Preference',    
    'ngRoute',
    'ngCookies',
    'ngAria',
    'ngStorage'    
])
 
.config(['$routeProvider', function ($routeProvider) {

    $routeProvider
        .when('/login', {
            controller: 'LoginController',
            templateUrl: 'modules/authentication/views/login.html',
            hideMenus: true
        })
        
        .when('/home', {
            controller: 'HomeController',
            templateUrl: 'modules/home/views/home.html'
        })
        
         .when('/', {
            controller: 'BuildController',
            templateUrl: 'modules/build/views/build.html'
        })
 
        
         .when('/edit', {
            controller: 'EditorController',
            templateUrl: 'modules/editor/views/edit.html'
        })
        
        .otherwise({ redirectTo: '/login' });
}])
.constant("globalVariableSvc", { details: [] })
.run(['$rootScope', '$location', '$cookieStore', '$http','$localStorage',
    function ($rootScope, $location, $cookieStore, $http,$localStorage) {
        // keep user logged in after page refresh
     //   $rootScope.globals = $cookieStore.get('globals') || {};
        
		$rootScope.$on('$locationChangeStart', function (event, next, current) {
		                        // redirect to login page if not logged in
		                        if ($location.path() !== '/login' && !$rootScope.scmUser) {
		                                $location.path('/login');
		                        }
		                }); 

 
      
       
        
        $rootScope.datas = "";
        
       $rootScope.shwfromhist =false;
    }]);