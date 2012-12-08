var currentIndex = 0;
var transitions = 0;

$(document).ready(function() {
	stackTheDeck();
	flipToIndex(currentIndex);
	$("body").keydown(function(e) {
		keyListener(e);
	});
	$("#navigation a").click(function(e) {
		flipToIndex($(this).index());
	});
});

function stackTheDeck () {
	totalNumber = $('#cards .card').length;
	$('#cards .card').each(function(index) {
	    $(this).css('z-index', (totalNumber - index));
	});
}

function keyListener (e) {
	if(e.keyCode == 37) { // left
		flipLeft();
	}
	else if(e.keyCode == 39) { // right
		flipRight();
	}
}
function flipLeft() {
	if (currentIndex < 3){
		flipToIndex(currentIndex+1);
	}
}
function flipRight() {
	if (currentIndex > 0){
		flipToIndex(currentIndex-1);
	}
}
function flipToIndex(targetIndex){
	$currentCard = $('#cards .card').eq(currentIndex);
	$targetCard = $('#cards .card').eq(targetIndex);

	$('#cards .card').removeClass('posLeft posRight');
	$('#cards .card:gt('+targetIndex+')').removeClass('posLeft posCenter posRight').addClass('posRight');
	$('#cards .card:lt('+targetIndex+')').removeClass('posLeft posCenter posRight').addClass('posLeft');
	$currentCard.removeClass('posLeft posRight').addClass('posCenter');

	if (targetIndex > currentIndex) {
		//$currentCard.css('z-index', 200);
		//$targetCard.css('z-index', 100);
		$targetCard.removeClass('transitioning posRight').addClass('posCenter');
		$currentCard.addClass('transitioning');
		$currentCard.removeClass('posCenter').addClass('posLeft');
	}
	else if (targetIndex < currentIndex){
		//$currentCard.css('z-index', 100);
		//$targetCard.css('z-index', 200);
		$currentCard.addClass('posCenter');
		$targetCard.addClass('transitioning');
		$targetCard.removeClass('posRight').addClass('posCenter');
	};

	$('menu#navigation a').eq(currentIndex).removeClass('active');
	$('menu#navigation a').eq(targetIndex).addClass('active');
	currentIndex = targetIndex;
}