var startTime = 0;
var duration = 0;
var pomodoroTimer = 0;
var activeTask = 'A pomodoro App'

$(document).on("ajaxDone",  function() {
	setupTask(1, 'Pompom', 25);

	$(".tasktracker span").click(function(e) {
		toggleTaskTokens($(this));
	});
});
$(document).on("taskChange", function(event, taskUrl) {
	$.ajax({
		url: taskUrl,
		dataType: 'html',
		success: function(response) {
        	var tempElement = $('<div>');
        	$(tempElement).html(response);
        	if ($(tempElement).find('.loginForm').length > 0) {
        		location.reload();
        	}
        	else{
        		$('#currentTaskTitle').html($(tempElement).find('h1').html());
        		setupTask (taskUrl, $(tempElement).find('h1').html(), 25)

        	};
      },
		error: function(event, jqxhr, settings, exception) {
			errorTitle = 'Could not load task';
			$('#currentTaskTitle').html(errorTitle);
    	}
    });
});

function setupTask (id, title, duration) {
	$('#currentTaskTitle').html(title);
	$('#currentTaskTitle').wrap('<a href="' + id + '" title="goto ' + title + ' summary page">');
	startTimer(duration);
}
function startTimer(durationInMinutes) {
	stopTimer();
	startTime = $.now();
	duration = durationInMinutes*60;
	pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 100);
}
function checkAndRestartPomodoroTimer () {
	var now = new Date();
	var elapsedSeconds = (now - startTime)/1000;
	if (elapsedSeconds < duration) {
		updateTimeGrade(elapsedSeconds)
		pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 200);
	}
	else{
		stopTimer();
	};
}
function updateTimeGrade (elapsedTime) {
	var margin = ((duration-elapsedTime)*0.01666666666666667)+0.1;
	$('.timegradeholder').css('margin-left', '-'+margin+'rem');
}
function stopTimer () {
	clearTimeout(pomodoroTimer);
	if (startTime > 0) {
		document.getElementById('alarm').play();
		console.log('ping');
	};
}
function toggleTaskTokens (token) {
	token.toggleClass('done');
}