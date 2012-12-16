var task = {id:'null', title:'', taskLength:'0'};

$(document).ready(function() {
	$(".addTaskSection button").click(function(e) {
		createNewTaskInList();
	});
	$('.tasks li').click(function(e) {
		activateTask($(this));
	});
});

function createNewTaskInList () {
	var title = $.trim($('.addTaskSection .title').val());
	var pomodoros = $.trim($('.addTaskSection .pomodoros').val());
	var description = $.trim($('.addTaskSection .shortDescription').val());

	if (title) {
		var newLi = $('li');
		console.log(newLi)
		$('ul.tasks').append('<li class="task"><label><span class="title">'+title+'</span><span class="pomMarker">'+pomodoros+'</span></label><div class="description">'+description+'</div></li>')
		$('.tasks li').click(function(e) {
			activateTask($(this));
		});
	}
	else {
		alert('You need a title');
	};
	
}
function activateTask (task) {
	if (task.hasClass('active')) {
		$(task).removeClass('active');
	}
	else{
		$('.tasks li').removeClass('active');
		$(task).addClass('active');
	};
}

function createNewTask (id, title, taskLength) {
	$task = $(task);
	$task.prop('id' , id);
	$task.prop('title' , title);
	$task.prop('taskLength' , taskLength);
	return $task;
}