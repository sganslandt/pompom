define(['jquery','sortable'], function($) {

	$(document).ready(function($) {
		$('.sortable').sortable({
			connectWith: '.connected'
		});
		$('.sortable li li').each(function( index ) {
			if ($(this).hasClass('interrupted')) {
				$(this).html('<img src="assets/img/icon_interrupted.svg" />');
			}
			else if ($(this).hasClass('broken')) {
				$(this).html('<img src="assets/img/icon_broken.svg" />');
			}
		});
	});
});