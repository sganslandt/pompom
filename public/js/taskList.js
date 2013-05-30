define(['jquery','sortable'], function($) {

	$(document).ready(function($) {
		sortablize();
		$("#tasks h2").click(function(e)
		{
      $("#tasks section").removeClass('active');
      $(this).closest('section').addClass('active');
  	});
  	$("#tasks #inventory li").click(function(e)
		{
      //moveTaskToList($(this), $('#today ol.taskList'));
  	});
	});

	function sortablize(list) {
		if (! list) {
			list = $('.sortable');
		};
		$(list).sortable({
			placeholder: "sortable-placeholder",
			revert: "100"
		});
		$(list).find('.pomodoros li').each(function( index ) {
			if ($(this).hasClass('broken')) {
				$(this).html('<img src="assets/img/icon_broken.svg" />');
			}
			else if ($(this).hasClass('interrupted')) {
				$(this).html('<img src="assets/img/icon_interrupted.svg" />');
			}
			else if ($(this).hasClass('inprogress')) {
				$(this).html('<img src="assets/img/icon_inprogress.svg" />');
			}
		});
	}
	function moveTaskToList (task, list) {
		$(list).append($(task));
	}

	return {
		markAsInProgress: function(pomodoro) {
			if (! pomodoro) {
				pomodoro = $('#today .task .pomodoros li.fresh').first();
			};
			$(pomodoro).addClass('inprogress').html('<img src="assets/img/icon_inprogress.svg" />');
		},
		markAsBroken: function(pomodoro) {
			if (! pomodoro) {
				pomodoro = $('#today .task .pomodoros .inprogress');
			};
			if (! $(pomodoro).hasClass('broken')) {
				$(pomodoro).removeClass('active inprogress').addClass('broken').html('<img src="assets/img/icon_broken.svg" />');
			};
		},
		markAsInterrupted: function(pomodoro) {
			if (! pomodoro) {
				pomodoro = $('#today .task .pomodoros .inprogress');
			};
			if (! $(pomodoro).hasClass('interrupted')) {
				$(pomodoro).addClass('interrupted').html('<img src="assets/img/icon_interrupted.svg" />');
			};
		},
		addTaskToList: function (targetList, title, numberOfPoms) {
			$newLi = $('<li draggable="true" />');
			$newLi.append('<div class="task" data-taskId="temp100">');
			$newLi.find('.task').append('<h3>'+title+'</h3>');
			$newLi.find('.task').append('<ol class="pomodoros">');
			for (var i = numberOfPoms - 1; i >= 0; i--) {
				$newLi.find('.pomodoros').append('<li class="active">&nbsp;</li>');
			};
			$newLi.css("opacity", 0);
			$(targetList).append($newLi);
			//wait a bit with opacity or else the animation don't occur
			setTimeout(function(){$(targetList).find(">li").css("opacity", 1);}, 30);
			$(targetList).sortable('refresh')
		}
	}
});