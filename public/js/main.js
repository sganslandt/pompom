$(function() {
			$('.sortable').sortable();
			$('.handles').sortable({
				handle: '.handle'
			});
});
jQuery(document).ready(function($) {
	$('.sortable li').click(function() {
	  console.log("clicked"+$(this));
	});
	$('#taskCreator button.expand').click(function() {
	  $('#taskCreator form').toggleClass('expanded');
	});
});
