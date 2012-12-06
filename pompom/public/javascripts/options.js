var deafaultTaskLength = 25;
var deafaultShortBreakLength = 5;
var deafaultLongBreakLength = 20;

$(document).ready(function() {
	
	$("button.sub").click(function(e) {
		subtractTime(1, $(this));
	});
	$("button.add").click(function(e) {
		addTime(1, $(this));
	});
});

function subtractTime (amount, clickedButton) {
	var time = clickedButton.siblings("span.tickertime").html() - parseInt(amount);
	if (time > 0 ) {
		clickedButton.siblings("span.tickertime").html(time);
	}
	else{
		alert("You can only enter a time between 1 and 60 minutes");
	};
}
function addTime (amount, clickedButton) {
	var time = parseInt(clickedButton.siblings("span.tickertime").html()) + parseInt(amount);
	if (time < 61 ) {
		clickedButton.siblings("span.tickertime").html(time);
	}
	else{
		alert("You can only enter a time between 1 and 60 minutes");
	};
}