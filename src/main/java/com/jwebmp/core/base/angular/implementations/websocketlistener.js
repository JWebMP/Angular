JW_APP_NAME.directive('websocketjw', ['$rootScope', '$interval', '$timeout', function ($rootScope, $interval, $timeout) {
    return {
        link: function ($scope, element, attrs) {
     //       alert('adding to web socket group');
            if(element[0].hasAttribute('websocketdata')) {
       //         alert('setting up data');
                var newData = JSON.parse(element[0].getAttribute('websocketdata'));
                newData.websocketGroup = element[0].getAttribute('websocketgroup');
                newData.headers = jw.headers;
          //      alert('data done - ' + newData);
            }
        //    alert('new message');
            jw.websocket.newMessage('AddToWebSocketGroup', {
                'websocketGroup': element[0].getAttribute('websocketgroup'),
                'sessionid': jw.sessionstorage['sessionid'],
                'jwamsmk': jw.localstorage['jwamsmk'],
                'headers': jw.headers,
                'data': newData
            });

            element.on('$destroy', function () {
                try {
                 //  alert('removing from web socket group');
                    if(element[0].hasAttribute('websocketdata')) {
                     //   alert('setting up destroy data');
                        var newData = JSON.parse(element[0].getAttribute('websocketdata'));
                        newData.websocketGroup = element[0].getAttribute('websocketgroup');
                        newData.headers = jw.headers;
                //        alert('destroy data done - ' + newData);
                    }
                    jw.websocket.newMessage('RemoveFromWebSocketGroup', {
                        'websocketGroup': element[0].getAttribute('websocketgroup'),
                        'sessionid': jw.sessionstorage['sessionid'],
                        'jwamsmk': jw.localstorage['jwamsmk'],
                        'headers': jw.headers,
                        'data': newData
                    });
                } catch (e) {

                }
            });
        }
    };
}]);
