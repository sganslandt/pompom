	var task = {id:'null', title:'', taskLength:'0'};
	var currentTaskUrl = "id";
	var appBuilt = false;

	$(document).on("ajaxDone", function() {
		
	});
	$(document).on("taskChange", function(event, taskUrl) {
		currentTaskUrl = taskUrl;
	});
	$('.card').on("cardUpdated",  function(event, target, content) {
		bindAndFillTaskLinks();
	});

	function bindAndFillTaskLinks() {
		var tasks = $('.tasks li');
		for (var i = 0; i < tasks.length; i++) {
		if (($($(tasks)[i])).find('.taskinfo').length <= 0) {
			console.log('empty');
			$($(tasks)[i]).click(function(e) {
				e.preventDefault();
				highlightTask($(this));
			});
			var url = ($($(tasks)[i])).find('a').attr('href')
			var target = $($(tasks)[i]);
			$.ajax({
				url: url,
				context: target,
				dataType: 'html',
				success: function(response) {
					var tempElement = $('<div>');
					$(tempElement).html(response);
					var stripped = $(tempElement).find('main *').html();

					$(this).append('<div class="taskinfo"></div>');
					$(this).find('.taskinfo').html(stripped);
				},
				error: function(event, jqxhr, settings, exception) {
					
				}
		    });
		}
          else {

            $(document).trigger("newCard", [$(links[i])]);
            insertInMenu($(links[i]));
          };
        }
		
	}

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