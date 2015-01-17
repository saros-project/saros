var app = angular.module('wizard-addcontact', []);

app.controller('AddContactController', ['$scope', function ($scope) {

    $scope.addContact = function (contact) {
       __java_addContact(contact.jid, contact.nickname);
    };

    $scope.cancel = function () {
        __java_cancelAddContactWizard();
    };
}]);