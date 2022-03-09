jw.websocket = {};
jw.websocket.connected = false;
jw.websocket.pollTime = 5000;
jw.websocket.pollCount = 1;
//jw.websocket.queuedMessages = [];

var wsAddy = jw.rootAddress.replace('https://', 'wss://');
wsAddy = wsAddy.replace('http://', 'ws://');
jw.websocket.address = wsAddy + '/wssocket';
jw.websocket.authdataproviders = [];


jw.websocket.reconnect = function () {
    jw.websocket.connection = new WebSocket(jw.websocket.address);
    jw.websocket.connected = true;

    jw.websocket.connection.onmessage = function (e) {
        //console.log(e.data);
        try {
            if (e !== undefined && e.data !== 'Ok') {
                jw.actions.processResponse(JSON.parse(e.data), $scope, $parse, $timeout, $compile);
                if (Pace) {
                    Pace.stop();
                    $('body').removeClass('pace-running');
                }
                jw.isLoading = false;
            }
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ' + e.data);
            if(e.stack)
            {
                console.log(e.stack);
            }
        }
        if (e.data === 'Ok') {
            jw.isLoading = false;
            if (Pace) {
                Pace.stop();
                $('body').removeClass('pace-running');
            }
        }
        var cookies = document.cookie.split(";");
        for (var i = 0; i < cookies.length; i++) {
            var equals = cookies[i].indexOf("=");
            var name = equals > -1 ? cookies[i].substr(0, equals) : cookies[i];
            document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
        }
    };

    jw.websocket.connection.onopen = function (e) {
        console.log('Web Socket connected');
        jw.websocket.reconnectTimer.stop();
        jw.websocket.connected = true;
        //    if (jw.websocket.timer)
        //        jw.websocket.timer.start();

        WS_AUTH_DATA_PROVIDER_LOAD;

        JW_JAVASCRIPT;
    };

    jw.websocket.connection.onclose = function (e) {
        if (e !== undefined)
            console.log('on close ' + e);
        else
            console.log('on close - No Data Object');

        jw.websocket.connected = false;
        jw.websocket.reconnectTimer.start();
    };

    jw.websocket.connection.onerror = function (e) {
        console.log('on error ' + e.data);
        jw.websocket.connected = false;
    };

};

jw.websocket.reconnect();

jw.websocket.sendPlainTextMessage = function (a) {
    jw.websocket.newMessage('PlainText', {message: a})
};

jw.websocket.newMessage = function (type, data) {
    var news = {};
    news.action = type;
    news.data = data;
    if (jw.sessionid && jw.sessionid[0])
        news.data.sessionid = jw.sessionid[0].replace('JSESSIONID=', '');

    if (jw.localstorage && jw.localstorage.jwamsmk) {
        news.data.jwamsmk = jw.localstorage.jwamsmk;
    }
    if (Pace) {
        Pace.restart();
    }
    jw.isLoading = true;
    jw.websocket.connection.send(JSON.stringify(news));
    //jw.websocket.queuedMessages.splice(i, 1);
    //jw.websocket.queuedMessages.push(news);
};

jw.websocket.newMessageNow = function (type, data) {
    var news = {};
    news.action = type;
    news.data = data;
    if (jw.sessionid && jw.sessionid[0])
        news.data.sessionid = jw.sessionid[0].replace('JSESSIONID=', '');

    if (jw.localstorage && jw.localstorage.jwamsmk) {
        news.data.jwamsmk = jw.localstorage.jwamsmk;
    }
    if (Pace) {
        Pace.restart();
    }
    jw.isLoading = true;
    jw.websocket.connection.send(JSON.stringify(news));
};

/*
jw.websocket.timer = new DeltaTimer(function (time) {
    //alert('messages : ' + jw.websocket.queuedMessages);
    if (jw.websocket.queuedMessages.length > 0) {
        if (jw.websocket.connected) {
            try {
                var i = jw.websocket.queuedMessages.length;
                while (i--) {
                    if (Pace)
                    {
                        Pace.restart();
                    }
                    jw.isLoading = true;
                    jw.websocket.connection.send(JSON.stringify(jw.websocket.queuedMessages[i]));
                    jw.websocket.queuedMessages.splice(i, 1);
                }
            } catch (e) {
                //console.log("Error going through queued messages");
            }
        }
        else {
            jw.websocket.reconnect();
        }
    }
}, 500, jw.websocket.timer);
jw.websocket.timerobj = jw.websocket.timer.start();
*/


jw.websocket.reconnectTimer = new DeltaTimer(function (time) {
    if (!jw.websocket.connected) {
        //   jw.websocket.timer.stop();
        jw.websocket.reconnect();
        jw.websocket.pollCount++;
        jw.websocket.reconnectTimer.delay = Math.max(jw.websocket.pollCount * jw.websocket.pollTime, 0);
    } else {
        jw.websocket.pollCount = 1;
    }
}, 10000, jw.websocket.reconnectTimer);

jw.websocket.reconnectTimerObject = jw.websocket.reconnectTimer.start();
