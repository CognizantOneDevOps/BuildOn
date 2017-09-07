'use strict';

// declare modules
angular.module('Authentication',['angularjs-crypto','angular-jwt', 'ngMessages']);
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
        $rootScope.globals = $cookieStore.get('globals') || {};
        
       /* if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.authdata; // jshint ignore:line
        }*/
        if ($localStorage.currentUser) {
            $http.defaults.headers.common.Authorization = 'Bearer' + $localStorage.currentUser.token;
        }
 
       /* $rootScope.$on('$locationChangeStart', function (event, next, current) {
            // redirect to login page if not logged in
            if ($location.path() !== '/login' && !$rootScope.globals.currentUser) {
                $location.path('/login');
            }
        });*/
        
        
        // redirect to login page if not logged in and trying to access a restricted page
        $rootScope.$on('$locationChangeStart', function (event, next, current) {
            var publicPages = ['/login'];
            var restrictedPage = publicPages.indexOf($location.path()) === -1;
            if (restrictedPage && !$localStorage.currentUser) {
                $location.path('/login');
            }
        });
        
        
        $rootScope.datas = "";
        
       $rootScope.shwfromhist =false;
    }]);