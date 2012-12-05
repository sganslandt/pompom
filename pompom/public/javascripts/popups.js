

$(document).ready(function() {
	$("#currentTaskTitle").click(function(e) {
		openPopUp('takeNote');
	});
	$("button.closePopup").click(function(e) {
		closePopup();
	});
	$(".newNoteArea button.save").click(function(e) {
		saveNote();
	});
});

function openPopUp (popuptype) {
	$("#popups").addClass('active');
	switch (popuptype) { 
	    case 'takeNote': 
	        openTakeTaskNote();
	        break;
	    case 'dayInHistory': 
	        openDayInHistory();
	        break;
	    case 'addTask': 
	        openaddTask();
	        break;
	    default:
	        alert('No Popups');
	}
}
function openTakeTaskNote () {

	$("#taskNote h2").html('Notes on "'+activeTask+'"');
	$("#taskNote").addClass('active');
}
function closePopup () {
	$("#popups").removeClass('active');
	setTimeout(function() {$("#popups .popup").removeClass('active');}, 500);
	
}
function saveNote () {
	var timeStamp = new Date(),
	h = timeStamp.getHours(), 
	m = timeStamp.getMinutes();
	if (m < 10) {
		m = '0'+m;
	};


	var textInput = $.trim($('.newNoteArea textarea').val());

	if (textInput) {
		$("#taskNote ul.notes").append('<li class="newNote"></li>');
		$('.newNote').append('<h4 class="time">'+h+':'+m+'</h4>');
		$('.newNote').append('<p class="text">'+textInput+'</p>');
		$('.newNote').removeClass('newNote');
	}
}
