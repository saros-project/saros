var app = angular.module('app', ['ui.bootstrap']);

app.controller('ToolbarController', function ($scope) {
    $scope.connectButtonText = "Connect";

    $scope.connected = false;

    $scope.accounts = [];

    $scope.connect = function () {
        if (!$scope.connected) {
            if ($scope.accounts.length > 0) {
                $scope.showDisconnect();
                $scope.connectUser($scope.accounts[0]);
            } else {
                alert("Cannot connect because no account is configured.");
            }
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
        __java_connect(JSON.stringify(account));
    };

    $scope.showAddAccountWizard = function () {
        __java_showAddAccountWizard();
    };
});

app.controller('ContactListCtrl', function ($scope) {
    $scope.contacts = [];

    $scope.root = null;

    $scope.add = function (contact) {
        $scope.contacts.push({name: contact})
    };

    $scope.displayRoot = function (account) {
        $scope.root = account.username + '@' + account.domain;
    };

    $scope.clearAll = function () {
        $scope.contacts = [];
    };
});

__angular_setAccountList = function (accountList) {
    var exposedScope = angular.element(document.getElementById('toolbar')).scope();
    exposedScope.$apply(function () {
        exposedScope.accounts = accountList;
    })

};

__angular_displayContactList = function (contactList) {
    var exposedScope = angular.element(document.getElementById('contact-list')).scope();
    exposedScope.$apply(exposedScope.displayRoot(contactList.account));
    exposedScope.$apply(exposedScope.clearAll());
    contactList.contactList.forEach(function (user) {
        exposedScope.$apply(exposedScope.add(user.displayName));
    });
};