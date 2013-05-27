define('timer',['jquery', 'taskList'], function($, taskList) {
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
		  interruptPomodoro();
		  showNotification();
		});
	});

	

	function startTimer(durationInMinutes) {
		startTime = $.now();
		duration = durationInMinutes*60;
		updateTimeGrade(duration, 0);
		pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 1000);
		$('#timer').addClass('active');
		console.log($('#timer').attr('class'));
		if ($('#today .taskList li').length < 1) {
			taskList.addTaskToList($('#today .taskList'), 'Your New Task', 1, "I automagicaly created a task for you. You should probably edit it to fit your new task.<br /> If you don't know the <a href='http://www.pomodorotechnique.com/' target='_blank' title='Go to www.pomodorotechnique.com in a new window'>Pomodoro Technique</a> yet, you should watch the video there.");
		};
		taskList.markAsInProgress();
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
	}
	function breakPomodoro () {
    stopTimer();
    taskList.markAsBroken();
	}
	function interruptPomodoro () {
		taskList.markAsInterrupted();
	}
	return{
		
	}
});