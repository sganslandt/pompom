define('taskCreator',['jquery', 'taskList'], function($, taskList) {
	$(document).ready(function($) {
		$('#taskCreator button.expand').click(function() {
		  $('#taskCreator form').toggleClass('expanded');
		  openPupup();
		});
		focusForm();
	});

	function resetForm () {
		$('#taskCreator form').find("input[type=text], textarea, input[type=number]").val("");
	}
	function focusForm () {
		$('#taskCreator form').find('#createTaskTitle').focus();
	}
	function openPupup () {
		$newPop = $('<div class="popup"><div class="content"></div></div>');
		$newPop.find('.content').append('<h2>Create new pomodoros<h2/>');
		$('body').append($newPop);

		$('.popup').click(function()
		{
			closePopup();
		});
		$(".popup .content").click(function(e)
		{
        e.stopPropagation();
  	});
  	$('.popup .content').append($('#createTask'));
	}
	function closePopup () {
  	$('#taskCreator').append($('#createTask'));
		$('.popup').remove();
	}


	return{
		createTask: function(eventData)
		{
			if ($(eventData.currentTarget).find(':checkbox').prop('checked')) { var targetList = $('#inventory .taskList');}
      else{ var targetList = $('#today .taskList');}
    	taskList.addTaskToList(
        targetList,
      	$(eventData.currentTarget).serializeArray()[0].value,
      	$(eventData.currentTarget).serializeArray()[1].value,
      	$(eventData.currentTarget).serializeArray()[2].value
      );
      resetForm();
      focusForm();
		}
	}
});