var app = angular.module('app', ['ui.bootstrap', 'ng-context-menu']);

app.controller('ToolbarController', function ($scope) {
    $scope.connectButtonText = "Connect";

    $scope.connected = false;

    $scope.buttonDisabled = false;

    $scope.accounts = [];

    //TODO function name, maybe toggle connect?
    $scope.connect = function () {
        if (!$scope.connected) {
            if ($scope.accounts.length > 0) {
                $scope.connectUser($scope.accounts[0]);
            } else {
                alert("Cannot connect because no account is configured.");
            }
        } else {
            Saros.disconnect();
        }
    };

    $scope.showConnect = function () {
        $scope.connectButtonText = "Connect";
        $scope.connected = false;
        $scope.buttonDisabled = false;
    };

    $scope.showDisconnect = function () {
        $scope.connectButtonText = "Disconnect";
        $scope.connected = true;
        $scope.buttonDisabled = false;
    };

    $scope.showConnecting = function () {
        $scope.connectButtonText = "Connecting...";
        $scope.buttonDisabled = true;
    };

    $scope.showDisconnecting = function () {
        $scope.connectButtonText = "Disconnecting...";
        $scope.buttonDisabled = true;
    };

    $scope.connectUser = function (account) {
        Saros.connect(JSON.stringify(account));
    };

    $scope.showAddAccountWizard = function () {
        Saros.showAddAccountWizard();
    };

    $scope.showAddContactWizard = function () {
        Saros.showAddContactWizard();
    };

    Saros.on('setAccountList', function (accountList) {
        $scope.$apply( function () {
            $scope.accounts = accountList;
        });
    });

    Saros.on('setIsConnected', function (connected) {
        if (connected) {
            $scope.$apply($scope.showDisconnect);
        } else {
            $scope.$apply($scope.showConnect);
        }
    });

    Saros.on('setIsConnecting', function () {
        $scope.$apply($scope.showConnecting);
    });

    Saros.on('setIsDisconnecting', function () {
        $scope.$apply($scope.showDisconnecting);
    });
});

app.controller('ContactListCtrl', function ($scope) {
    $scope.contacts = [];

    $scope.root = null;

    $scope.selected = null;

    $scope.add = function (contact, presence, addition) {
        $scope.contacts.push({name: contact, presence: presence, addition: addition})
    };

    $scope.displayRoot = function (account) {
        $scope.root = account.username + '@' + account.domain;
    };

    $scope.clearAll = function () {
        $scope.contacts = [];
    };

    $scope.selectContact = function (name) {
        $scope.selected = name;
    };

    $scope.renameContact = function () {
        Saros.renameContact($scope.selected);
    };

    $scope.deleteContact = function () {
        Saros.deleteContact($scope.selected);
    };

    Saros.on('displayContactList', function (contactList) {

        $scope.$apply( function () {
            $scope.displayRoot(contactList.account);
            $scope.clearAll();
            contactList.contactList.forEach(function (contact) {
                $scope.add(contact.displayName, contact.presence, contact.addition);
            });
        });
    });
});