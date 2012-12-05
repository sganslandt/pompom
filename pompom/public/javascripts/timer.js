var startTime = 0;
var duration = 0;
var pomodoroTimer = 0;
var activeTask = 'A pomodoro App'


$(document).ready(function() {
	setupTask('task');
	
	$(".tasktracker span").click(function(e) {
		toggleTaskTokens($(this));
	});
});
function setupTask (type) {

	$newTask = createNewTask(1, 'A pomodoro app', 25);
	
	$('#currentTaskTitle').html($newTask.prop('id')+' '+$newTask.prop('title'));
	startTimer($newTask.prop('taskLength'));
}
function startTimer(durationInMinutes) {
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
	$('.timegradeholder').css('margin-left', '-'+margin+'em');
}
function stopTimer () {
	clearTimeout(pomodoroTimer);
	document.getElementById('alarm').play();
	console.log('ping');
}
function toggleTaskTokens (token) {
	token.toggleClass('done')
}