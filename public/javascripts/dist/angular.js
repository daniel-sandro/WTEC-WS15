var myApp = angular.module('myApp', []);

myApp.directive('profileApp', function() {
    return {
        restrict: 'AECM',
        replace: true,
        templateUrl: '../../elements/profile.html'
    };
});