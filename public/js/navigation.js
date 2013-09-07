define(['jquery'], function ($) {
    "use strict";
    var routes = {
            '/': '#timer',
            '/timer': '#timer',
            '/today': '#today',
            '/inventory': '#inventory',
            '/settings': '#settings'
        },
        animationTime = 250,
        popped = false,
        initialURL = window.location.href;

    $(document).ready(function ($) {
        if (!routes[window.location.pathname]) {
            navigateToPage('/', false);
        } else {
            setActivePage(window.location.pathname, false);
        }
        $('#mainNav').find('a.menu').click(function (event) {
            event.preventDefault();
            if ($(this).attr('href') !== window.location.pathname) {
                navigateToPage($(this).attr('href'));
            }
        });
    });

    // Listen to History Popstate Event
    $(window).bind('popstate', function () {
        // Ignore inital popstate that some browsers fire on page load
        var initialPop = !popped && window.location.href === initialURL;
        popped = true;
        if (initialPop) {
            return;
        }
        if (!routes[window.location.pathname]) {
            navigateToPage('/');
        } else {
            setActivePage(window.location.pathname);
        }
    });

    function navigateToPage(targetStateURL, animate) {
        history.pushState({}, 'Pompom - ' + routes[targetStateURL].substring(1), targetStateURL);
        setActivePage(targetStateURL, animate);
    }

    function setActivePage(targetStateURL, animate) {
        var $main = $('main'),
            $mainNav = $('#mainNav'),
            route = routes[targetStateURL];

        if (animate === undefined) {
            animate = true;
        }
        if (animate) {
            $main.find('section.active').addClass('slide-back');
            setTimeout(function () {
                $main.find('section').removeClass('active slide-in slide-back');
                $main.find(route).addClass('active slide-in');
            }, animationTime);
        } else {
            $main.find('section').removeClass('active slide-in slide-back');
            $main.find(route).addClass('active');
        }

        // Set Current in menu
        $mainNav.find('a').removeClass('current');
        $mainNav.find('a.' + route.substring(1) + 'Link').addClass('current');
    }
});