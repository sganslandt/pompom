define('responsive', ['jquery', 'taskCreator'], function ($, taskCreator) {
    "use strict";
    var updateTimer,
        phones = 0,
        largePhones = 481,
        tablet = 768,
        desktopSmall = 980,
        desktopMedium = 1200,
        desktopLarge = 1600;

    $(document).ready(function () {
        $(window).resize(function () {
            window.clearTimeout(updateTimer);
            updateTimer = setTimeout(function () {
                updateView($(window).width());
            }, 250);
        });
    });

    function getSizeName(width) {
        if (width < largePhones) {
            return "phones";
        }
        if (width < tablet) {
            return "largePhones";
        }
        if (width < desktopSmall) {
            return "tablet";
        }
        if (width < desktopMedium) {
            return "desktopSmall";
        }
        if (width < desktopLarge) {
            return "desktopMedium";
        }
        return "desktopLarge";
    }

    function updateView(viewportSize) {
        //console.log($('.popup :focus').length);
        if (viewportSize >= desktopSmall) {
            if ($('.popup').length > 0) {
                taskCreator.closeCreateFormPopup();
            }
        }
    }
});