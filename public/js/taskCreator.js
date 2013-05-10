define('taskCreator',['jquery'], function($) {
	$(document).ready(function($) {
		$('#taskCreator button.expand').click(function() {
		  $('#taskCreator form').toggleClass('expanded');
		});
	});
	
});