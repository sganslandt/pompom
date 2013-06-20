define(['jquery'], function ($) {
    var notificationOpen = false;

    $(document).ready(function ($) {
        
    });

    function authorizeDesktopNotification () {
        Notification.requestPermission();
    }
    function showDesktopNotification (title, content, imageURL) {
        var notification = new Notification(title, {
            dir: "auto",
            lang: "",
            body: content,
            tag: "sometag",
            type: "basic",
            iconUrl: imageURL
        });
    }
    function newNotificationBar (message)
    {
        if (!notificationOpen) {
            notificationOpen = true;
            if (!content) {var content = 'Move along folk'};
            var $newBar = $('<div id="notification-bar" class="slide-down"><div class="notification"><button title="close notification" class="close-button">x</button><p class="content">' + message + '</p></div></div>');
            $('body').append($newBar);
            $('#notification-bar').click(function (e) { e.stopPropagation(); });
            $('#notification-bar button').click(function () {
                closeNotificationBar();
            });
            setTimeout(function(){
                closeNotificationBar();
            }, 5000);
        }
    }
    function closeNotificationBar() {
            $('#notification-bar').removeClass().addClass('slide-back-up');
            setTimeout(function(){
                $('#notification-bar').remove();
            }, 250);
            notificationOpen = false;
    }
    return {
        desktop: function (title, content, imageURL) {
            showDesktopNotification (title, content, imageURL);
        },
        authorize: function() {
            authorizeDesktopNotification();
        },
        bar: function(message) {
            newNotificationBar(message);
        }
    }

});