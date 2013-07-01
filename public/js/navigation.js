define(['jquery', 'modal'], function ($, modal) {
	var Routes = {
		'/' : '#timer',
		'/timer' : '#timer',
		'/today' : '#today', 
		'/inventory': '#inventory', 
		'/settings': '#settings'
	};
	var AnimationTime = 250;
	var popped = false;
	var initialURL = window.location.href;

	$(document).ready(function ($) {
		if(!Routes[window.location.pathname])
		{
			navigateToPage('/', false);
		}
		else
		{
			setActivePage(window.location.pathname, false);
		}
		$('#mainNav a.menu').click(function (event){
			event.preventDefault();
			if ($(this).attr('href') != window.location.pathname) {
				navigateToPage($(this).attr('href'));
			};
		});
	});

	// Listen to History Popstate Event
	$(window).bind('popstate', function(event){
		// Ignore inital popstate that some browsers fire on page load
		var initialPop = !popped && window.location.href == initialURL;
		popped = true;
		if ( initialPop ) return;
		if(!Routes[window.location.pathname])
		{
			navigateToPage('/');
		}
		else
		{
			setActivePage(window.location.pathname);
		};
	});

	function navigateToPage (targetStateURL, animate) {
		history.pushState({}, 'Pompom - ' + Routes[targetStateURL].substring(1), targetStateURL);
		setActivePage(targetStateURL, animate);
	}

	function setActivePage (targetStateURL, animate) {
		var $main = $('main');
		var $mainNav = $('#mainNav');
		var route = Routes[targetStateURL];
		if (typeof animate === 'undefined')
		{
			animate = true;
		};
		if (animate)
		{
			$main.find('section.active').addClass('slide-back');
			setTimeout(function()
			{
				$main.find('section').removeClass('active slide-in slide-back');
				$main.find(route).addClass('active slide-in');
			}, AnimationTime);
		}
		else
		{
			$main.find('section').removeClass('active slide-in slide-back');
			$main.find(route).addClass('active');
		};
		
		// Set Current in menu
		$('#mainNav a').removeClass('current');
		$('#mainNav a.' + route.substring(1) + 'Link').addClass('current');
	}
});