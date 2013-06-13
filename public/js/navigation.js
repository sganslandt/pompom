define(['jquery'], function ($) {
	var Routes = {
		'/' : '#today',
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
		$('#mainNav a').click(function (event){
			event.preventDefault();
			navigateToPage($(this).attr('href'));
		});
	});

	function navigateToPage (targetStateURL) {
		history.pushState({}, 'Pompom - ' + Routes[targetStateURL].substring(1), targetStateURL);
		setActivePage(targetStateURL);
	}

	function setActivePage (targetStateURL) {
		$main = $('main');
		$mainNav = $('#mainNav');
		var route = Routes[targetStateURL]
		/*if (!Routes[targetStateURL])
		{
			var route = Routes['/'];
		}
		else
		{
			var route = Routes[targetStateURL];
		};*/

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

});