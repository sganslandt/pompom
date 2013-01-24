	var task = {id:'null', title:'', taskLength:'0'};
	var currentTaskUrl = "null";

	$(document).on("ajaxDone", function() {
		$(".addTaskSection button").click(function(e) {
			createNewTaskInList();
		});
		$('.tasks li').click(function(e) {
			e.preventDefault();
			highlightTask($(this));
		});
	});
	$(document).on("taskChange", function(event, taskUrl) {
		currentTaskUrl = taskUrl;
	});

	function createNewTaskInList () {
		var title = $.trim($('.addTaskSection .title').val());
		var pomodoros = $.trim($('.addTaskSection .pomodoros').val());
		var description = $.trim($('.addTaskSection .shortDescription').val());

		if (title) {
			var newLi = $('li');
			$('ul.tasks').append('<li class="task"><label><span class="title">'+title+'</span><span class="pomMarker">'+pomodoros+'</span></label><div class="description">'+description+'</div></li>')
			$('.tasks li').click(function(e) {
				highlightTask($(this));
			});
		}
		else {
			alert('You need a title');
		};
		
	}
	function highlightTask (task) {
		if (task.hasClass('active')) {
			$(task).removeClass('active');
			$(document).trigger("taskChange", ['null']);
		}
		else{
			$('.tasks li').removeClass('active');
			$(task).addClass('active');
			$(document).trigger("taskChange", [$(task).find('a').attr('href')]);
		};
	}

	function createNewTask (id, title, taskLength) {
		$task = $(task);
		$task.prop('id' , id);
		$task.prop('title' , title);
		$task.prop('taskLength' , taskLength);
		return $task;
	}