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
function markAsInProgress() {
	$('#today .task .pomodoros li.active').first().addClass('inprogress');
}
function markAsBroken () {
	activeTask = $('#today .task .inprogress').removeClass('active inprogress').addClass('broken').html('<img src="assets/img/icon_broken.svg" />');
}
function markAsInterrupted () {
	activeTask = $('#today .task .inprogress').addClass('interrupted').html('<img src="assets/img/icon_interrupted.svg" />');
}