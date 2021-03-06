(function() {

    'use strict';

    var appProperties = {
        demoMode: true,
        ephiProhibited: true,
        registrationEnabled: true,
        aiwSiteUrl: 'http://aiw.sourceforge.net',
        helpSiteUrl: 'http://aiw.sourceforge.net/help/v1-10',
        organizationName: 'Emory University',
        apiEndpoint: '/eureka-services/api/protected',
        dataEndpoint: '/eureka-webapp/proxy-resource',
        filterEndpoint: '/eureka-webapp/protected/jstree3_searchsystemlist'
    };

    /**
     * @ngdoc object
     * @name eureka.appProperties
     * @description
     * Simple configuration object that stores application properties.
     */

    angular.module('eureka')
        .value('appProperties', appProperties);

}());