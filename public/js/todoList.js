define(['jquery','sortable'], function($) {

	$(document).ready(function($) {
		$('.sortable').sortable({
			connectWith: '.connected'
		});
		/*$('.handles').sortable({
			handle: '.handle'
		});

		$('.sortable li').click(function() {
		  console.log("clicked"+$(this));
		});*/
	});
});