(function() {
  var httpRequest;
  var url = 'http://app.pompom.nu:9000/api';
  var pomodoroTimer = 0;
  var userLinkId = "currentUser";

	$(document).ready(function() {
    fetchApiAndBuild ();
	});

  function fetchApiAndBuild () {
    $.get(url, function(data) {
      var links = $(data).find('a');
      for (var i = 0; i < links.length; i++) {
        if ($(links[i]).attr("id") == userLinkId) {
          createCard($(links[i]));
          $('#userId').attr('id', userLinkId + 'Link');
        }
        else if ($(links[i]).attr("id") == "tasks") {
          createCard($(links[i]));
          insertInMenu($(links[i]));
        }
        else {
          createCard ($(links[i]))
          insertInMenu($(links[i]));
        };
      };
      buildDeck();
    });
  }

  function createCard (object) {
    var cardId = $(object).attr("id") + 'Card';
    insertBeforeIfPossible("#Deck", '<section id="' + cardId + '" class="card"></section>', '#' + userLinkId + 'Card');
    ajaxRequest ($(object).attr("href"), '#' + cardId);
  }


  function insertInMenu (object) {
    var newLinkId = $(object).attr("id") + 'Link';
    $(object).attr('id', newLinkId);
    $(object).removeAttr("href");
    insertBeforeIfPossible("#MainMenu", object, '#' + userLinkId + 'Link')
  }


  function insertBeforeIfPossible (parent, object, target) {
    if ($(target).length > 0 && object != target) {
      $(target).before(object);
    }
    else {
      $(parent).append(object);
    };
  }

  function ajaxRequest (url, target) {
    // TODO Get built in function to work
  
    $.ajax({
      url: url,
      dataType: 'html',
      success: function(response) {
        var tempElement = $('<div>');
        $(tempElement).html(response);
        var stripped = $(tempElement).find('main').html();
        $(target).html(stripped);
      }
    });
    
    // This is the built in jQuery version of doing the above. It has some problems.
    // The DOM breaks and forms, lis and similar elements are closed before their children are placed.
    /*
    $(target).html().load(url + ' main *');
    */
  }

 
}());