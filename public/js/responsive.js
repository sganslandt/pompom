define('responsive',['jquery', 'taskCreator'], function($, taskCreator)
{
	var updateTimer;

	var phones = 0;
	var largePhones = 481;
	var tablet = 768;
	var desktopSmall = 980;
	var desktopMedium = 1200;
	var desktopLarge = 1600;

	$(document).ready(function ()
	{
	    $(window).resize(function() {
	    	window.clearTimeout(updateTimer);
	    	updateTimer = setTimeout(function(){updateView($(window).width());}, 250);
	    });
	});

	function getSizeName (width) {
		if (width < largePhones) {return "phones"};
		if (width < tablet) {return "largePhones"};
		if (width < desktopSmall) {return "tablet"};
		if (width < desktopMedium) {return "desktopSmall"};
		if (width < desktopLarge) {return "desktopMedium"}
		else {return "desktopLarge"};
	}
	function updateView (viewportSize) {
		//console.log($('.popup :focus').length);
		if (viewportSize >= desktopSmall){
			if ($('.popup').length > 0) {
				if (true) {};
				taskCreator.closeCreateFormPopup()
			};
		};
	}
});