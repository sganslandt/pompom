define(['jquery','sortable'], function($) {

	$(document).ready(function($) {
		sortablize();
	});

	function sortablize(list) {
		if (! list) {
			list = $('.sortable');
		};
		$(list).sortable({
			connectWith: ".connected",
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

	return {
		markAsInProgress: function(pomodoro) {
			if (! pomodoro) {
				pomodoro = $('#today .task .pomodoros li.active').first();
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
		addTaskToList: function (targetList, title, numberOfPoms, description) {
			$newLi = $('<li draggable="true" />');
			$newLi.append('<div class="task" data-taskId="temp100">');
			$newLi.find('.task').append('<h3>'+title+'</h3>');
			$newLi.find('.task').append('<p>'+description+'</p>');
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