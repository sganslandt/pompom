define('taskCreator',['jquery'], function($) {
	$(document).ready(function($) {
		$('#taskCreator button.expand').click(function() {
		  $('#taskCreator form').toggleClass('expanded');
		});
		$('#taskCreator form').find('#createTaskTitle').focus();
	});


	return{
		resetForm: function() {
			$('#taskCreator form').find("input[type=text], textarea, input[type=number]").val("");
      $('#taskCreator form').find('#createTaskTitle').focus();
		}
	}
});