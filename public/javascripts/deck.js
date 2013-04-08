(function() {
	var currentIndex = 0;
	var transitions = 0;

	$(document).on("ajaxDone",  function() {
		buildDeck ();
	});
	$(document).on("newCard",  function(event, link) {
		createCard(link)
	});

	$('.card').on("cardUpdated",  function(event, target, content) {
		updateCard (target, content);
	});

	function buildDeck () {
		stackTheDeck();
		flipToIndex(currentIndex);
		$("body").keydown(function(e) {
			keyListener(e);
		});
		$("#MainMenu a").click(function(e) {
			flipToIndex($(this).index());
		});
	}

	function stackTheDeck () {
		totalNumber = $('#Deck .card').length;
		$('#Deck .card').each(function(index) {
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
		$currentCard = $('#Deck .card').eq(currentIndex);
		$targetCard = $('#Deck .card').eq(targetIndex);

		$('#Deck .card').removeClass('posLeft posRight');
		$('#Deck .card:gt('+targetIndex+')').removeClass('posLeft posCenter posRight').addClass('posRight');
		$('#Deck .card:lt('+targetIndex+')').removeClass('posLeft posCenter posRight').addClass('posLeft');
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

		$('menu#MainMenu a').eq(currentIndex).removeClass('active');
		$('menu#MainMenu a').eq(targetIndex).addClass('active');
		currentIndex = targetIndex;
	}
	function createCard (object) {
		var cardId = $(object).attr("id") + 'Card';
		insertBeforeIfPossible("#Deck", '<section id="' + cardId + '" class="card" data-url="' + $(object).attr("href") + '"></section>', '#' + userLinkId + 'Card');
		fillCard (cardId, $(object).attr("href"));
	}
	function fillCard (cardId, url) {
		$(document).trigger("requestAjaxRequestForMain", [url, cardId])
	}
	function updateCard (target, content) {
		$('#' + target).html(content);
		if ($('#' + target + ' form').length > 0) {
			bindForm($('#' + target + ' form'));
    	}
	}
	function bindForm (form) {
	    $(form).submit(function(e) { 
			e.preventDefault();
			$.ajax({
				url   : form.attr('action'),
				type  : form.attr('method'),
				data  : form.serialize(),
				success: function(response){
					var target = $(form).closest(".card");
					if ($(target).length > 0) {
						fillCard($(target).attr('id'), $(target).data('url'));
					}
					else {
						console.log('no card');
					}
				}
			});
			return false;
		});
	}
	function insertBeforeIfPossible (parent, object, target) {
		if ($(target).length > 0 && object != target) {
			$(target).before(object);
		}
		else {
			$(parent).append(object);
		};
	}
}());