define(['jquery', 'modal', 'notify'], function ($, modal, notify) {
	var Routes = {
		'/' : '#timer',
		'/timer' : '#timer',
		'/today' : '#today', 
		'/inventory': '#inventory'
	};
	var AnimationTime = 250;

	$(document).ready(function ($) {
		if(!Routes[window.location.pathname])
		{
			navigateToPage('/');
		}
		else
		{
			setActivePage(window.location.pathname);
		}
		$('#mainNav a.menu').click(function (event){
			event.preventDefault();
			if ($(this).attr('href') != window.location.pathname) {
				navigateToPage($(this).attr('href'));
			};
		});
		$('#mainNav a.settingsLink').click(function (event){
			event.preventDefault();
			openSettings ();
		});
	});

	// Listen to History Popstate Event
	window.addEventListener('popstate', function(event) {
		if(!Routes[window.location.pathname])
		{
			navigateToPage('/');
		}
		else
		{
			setActivePage(window.location.pathname);
		}
	});

	function navigateToPage (targetStateURL) {
		history.pushState({}, 'Pompom - ' + Routes[targetStateURL].substring(1), targetStateURL);
		setActivePage(targetStateURL);
	}

	function setActivePage (targetStateURL) {
		$main = $('main');
		$mainNav = $('#mainNav');
		var route = Routes[targetStateURL]


		$main.find('section.active').addClass('slide-back');
		setTimeout(function()
		{
			$main.find('section').removeClass('active slide-in slide-back');
			$main.find(route).addClass('active slide-in');
		}, AnimationTime);
		// Set Current in menu
		$('#mainNav a').removeClass('current');
		$('#mainNav a.' + route.substring(1) + 'Link').addClass('current');
	}

	function openSettings () {
		var title = 'Settings';
        var content =  "<a id='logout' href='/logout'>Logout of Pompom</a> \
                        <hr> \
                        <button class='authorizeNotification'>Enable Desktop Notifications</button>";
 		modal.new(title, content);
 		$(".modal button.authorizeNotification").click(function () {
            notify.authorize();
        });
	}

});