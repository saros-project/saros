/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

var app = angular.module('app', ['ui.bootstrap']);

app.controller('ToolbarController', function ($scope) {
    $scope.connectButtonText = "Connect";

    $scope.connected = false;

    $scope.accounts = [];

    $scope.connect = function () {
        if (!$scope.connected) {
            $scope.showDisconnect();
            __java_connect();
        } else {
            $scope.connectButtonText = "Connect";
            __java_disconnect();
            $scope.connected = false;
        }
    };

    $scope.showDisconnect = function () {
        $scope.connectButtonText = "Disconnect";
        $scope.connected = true;
    };

    $scope.connectUser = function (account) {
        $scope.showDisconnect();
        __java_connect(account);
    };

    $scope.showAddAccountWizard = function () {
        __java_showAddAccountWizard();
    };
});

__angular_setAccountList = function (accountList) {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    exposedScope.$apply(function () {
        exposedScope.accounts = accountList;
    })
};