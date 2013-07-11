define(['jquery'], function ($) {
    var notificationOpen = false;
    var notificationQueue = [];
    var closeBarTimer;
    var permissions = [
        'granted', 
        'default',
        'denied'
    ];
    isSupported = (function () {
        var isSupported = false;
        try {
            isSupported = !!(Notification || webkitNotifications);
        } catch (e) {}
        return isSupported;
    }()),

    $(document).ready(function ($) {
        if(isSupported && checkPermission() == 'granted')
            disableDesktopNotificationButton ();
    });

    function authorizeDesktopNotification () {
        try {
            Notification.requestPermission();
        }
        catch (error) {
            queueNotificationBar("Notifications are not supported in your browser");
        }
    }
    function showDesktopNotification (title, content, imageURL) {
        console.log(checkPermission());
        if (checkPermission() == 'granted')
        {
            try {
                notification = new Notification(title, {
                dir: "auto",
                lang: "en",
                body: content,
                tag: "pompom",
                type: "basic",
                replaceId: "pompomNotification",
                iconUrl: "../assets/img/icon/64.png"
                });
            }
            catch (error) {
            }
        } 
        else{
            queueNotificationBar (title + ': ' + content + ' | <a href="/settings">desktop notifications</a>')
        };
    }
    function checkPermission(){
        var permission;
        if (!isSupported) { return; }
        if (window.Notification && window.Notification.permissionLevel) {
            //Safari 6
            permission = window.Notification.permissionLevel();
        } else if (window.webkitNotifications && window.webkitNotifications.checkPermission) {
            //Chrome
            permission = permissions[webkitNotifications.checkPermission()];
        } else if (navigator.mozNotification) {
            //Firefox Mobile
            permission = 'granted';
        } else if (window.Notification && window.Notification.permission) {
            // Firefox 23+
            permission = window.Notification.permission;
        }
        return permission;
    }
    function disableDesktopNotificationButton () {
        $('.authorizeNotification').attr('disabled', 'disabled');
    }
    function closeDesktopNotification()
    {
        // Does not seem to be implemented yet
    }
    function queueNotificationBar (message)
    {
        notificationQueue.push(message);
        if (!notificationOpen){
            newNotificationBar();
        }
    }
    function newNotificationBar ()
    {
        notificationOpen = true;
        var $newBar = $('<div id="notification-bar" class="slide-down"><div class="notification"><button title="close notification" class="close-button">x</button><p class="content"><img src="/assets/img/icon/32.png" />' + notificationQueue[0] + '</p></div></div>');
        notificationQueue.shift();
        $('body').append($newBar);
        $('#notification-bar').click(function (e) { e.stopPropagation(); });
        $('#notification-bar button').click(function () {
            closeNotificationBar();
        });
        closeBarTimer = setTimeout(function(){
            closeNotificationBar();
        }, 5000);

    }
    function closeNotificationBar() {
        clearTimeout(closeBarTimer);
        $('#notification-bar').removeClass().addClass('slide-back-up');
        setTimeout(function(){
            $('#notification-bar').remove();
            notificationOpen = false;
            if (notificationQueue.length > 0) {newNotificationBar();};
        }, 250);
    }
    return {
        desktop: function (title, content, imageURL) {
            showDesktopNotification (title, content, imageURL);
        },
        authorize: function() {
            showDesktopNotification('title', 'body');
            //authorizeDesktopNotification();
        },
        bar: function(message) {
            queueNotificationBar(message);
        }
    }

});