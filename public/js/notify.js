define(['jquery'], function ($) {
    var notificationOpen = false;
    var notificationQueue = [];
    var closeBarTimer;

    $(document).ready(function ($) {
        checkPermission();
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
            checkPermission(notification);
        }
        catch (error) {
            authorizeDesktopNotification ();
        }
    }
    function checkPermission(notification){
        // Does not seem to be implemented yet
        if (notification) {
            console.log(notification.permission);
            if(notification.permission == 'default'){
                queueNotificationBar("You can enabled desktop notification under settings");
            }
            else if(notification.permission == 'granted'){
                disableDesktopNotificationButton();
            };
        };
        try {
            Notification.requestPermission();
        }
        catch (error) {
            disableDesktopNotificationButton();
        }
    }
    function disableDesktopNotificationButton (argument) {
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
            authorizeDesktopNotification();
        },
        bar: function(message) {
            queueNotificationBar(message);
        }
    }

});