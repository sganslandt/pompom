var currentIndex = 0;

$(document).ready(function() {

});

function keyListener (e) {
	if(e.keyCode == 37) { // left
		scrollLeft();
	}
	else if(e.keyCode == 39) { // right
		scrollRight();
	}
}
function scrollLeft() {
	if (currentIndex < 3){
		scrollToIndex(currentIndex+1);
	}
}
function scrollRight() {
	if (currentIndex > 0){
		scrollToIndex(currentIndex-1);
	}
}
function scrollToIndex(targetIndex){
	var percentage = (targetIndex*100)
	$('#cards #slider').removeClass().addClass('pos'+percentage)
	$('menu#navigation a').eq(currentIndex).removeClass('active');
	$('menu#navigation a').eq(targetIndex).addClass('active');
	currentIndex = targetIndex;
}