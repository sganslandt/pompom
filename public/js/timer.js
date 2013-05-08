define('timer',['jquery'], function($) {
	var startTime = 0;
	var duration = 0;
	var pomodoroTimer = 0;
	var defaultDuration = 25;

	$(document).ready(function($) {
		$('button#startPomodoro').click(function() {
		  startTimer(defaultDuration);
		});
		$('button#breakPomodoro').click (function(){
		    if (confirm('Do you really want to break this pomodoro')){
		    breakPomodoro();
		    }
		});
		$('button#interruptPomodoro').click(function() {
		  registerInterruption();
		});
	});

	function startTimer(durationInMinutes) {
		startTime = $.now();
		duration = durationInMinutes*60;
		updateTimeGrade(duration, 0);
		pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 1000);
		$('#timer').addClass('active');
		console.log('timer starts');
	}

	function checkAndRestartPomodoroTimer () {
		var now = new Date();
		var elapsedSeconds = (now - startTime)/1000;
		if (elapsedSeconds < duration) {
			updateTimeGrade(duration, elapsedSeconds);
			pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 200);
		}
		else{
			stopTimer();
		};
	}

	function updateTimeGrade (duration, elapsedTime) {
		var margin = ((duration-elapsedTime)*0.01666666666666667)+0.1;
		$('.timegradeholder').css('margin-left', '-'+margin+'rem');
	}
	function stopTimer () {
		$('#timer').removeClass('active');
		clearTimeout(pomodoroTimer);
		updateTimeGrade(1, 1);
		document.getElementById('alarm').play();
		console.log('rrrrrrrring! timer stops');
	}
	function breakPomodoro () {
    stopTimer();
	}
	function registerInterruption (argument) {
		
		// body...
	}
	return{
		
	}
});