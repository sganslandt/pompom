  var httpRequest;
  var url = 'http://app.pompom.nu:9000/api';
  var pomodoroTimer = 0;
  var userLinkId = "currentUser";
  var APICache = $('<div id="API"></div>');

	$(document).ready(function() {
    fetchApiAndBuild ();
	});

$(document).ajaxError(function(event, request, settings) {
  console.log( "Error requesting " + settings.url );
  console.log(request.status + ' ' + request.statusText);
});

  function fetchApiAndBuild () {

    $.ajax({
      url: url,
      dataType: 'html',
      success: function(response) {
        var links = $(response).find('a');
        for (var i = 0; i < links.length; i++) {
          if ($(links[i]).attr("id") == userLinkId) {
            createCard($(links[i]));
            $('#userId').attr('id', userLinkId + 'Link');
          }
          else {
            createCard ($(links[i]));
            insertInMenu($(links[i]));
          };
        }
        buildDeck();
      }
    });
  }
  function updateApiAndBuild () {
    $.get(url, function(data) {
      var links = $(data).find('a');
      for (var i = 0; i < links.length; i++) {
        fillCard ($(links[i]).attr("id") + 'Card', $(links[i]).attr("href"));
      };
    });
  }


  function createCard (object) {
    var cardId = $(object).attr("id") + 'Card';
    insertBeforeIfPossible("#Deck", '<section id="' + cardId + '" class="card" data-url="' + $(object).attr("href") + '"></section>', '#' + userLinkId + 'Card');
    fillCard (cardId, $(object).attr("href"));
  }
  function fillCard (cardId, url) {
    console.log('filling ' + cardId + ' from ' + url)
    ajaxRequestForMain (url, cardId);
  }
  function updateCard (target, content) {
    $('#' + target).html(content);
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

  function ajaxRequestForMain (url, target) {
    // TODO Get built in function to work
    $.ajax({
      url: url,
      dataType: 'html',
      success: function(response) {
        var tempElement = $('<div>');
        $(tempElement).html(response);
        var stripped = $(tempElement).find('main').html();
        //$('#' + target).html(stripped);
        updateAPICache(target, stripped)
      },
      error: function(event, jqxhr, settings, exception) {
        errorTitle = '<h2>Oops, something went wrong</h2>'
        errorMessage = '<p>' + event.status + ' ' + event.statusText + '</p>'
        updateAPICache(target, errorTitle + errorMessage)
      }
    });
    // This is the built in jQuery version of doing the above. It has some problems.
    // The DOM breaks and forms, lis and similar elements are closed before their children are placed.
    /*
    $(target).html().load(url + ' main *');
    */
  }
  function updateAPICache(target, response) {
    if ($(APICache).find('#' + target).length > 0 && $(APICache).find('#' + target).html() == response) {
      
    }
    else if ($(APICache).find('#' + target).length > 0) {
      $(APICache).find('#' + target).html(response);
      updateCard(target, $(APICache).find('#' + target).html());
    }
    else{
      $(APICache).append('<div id="' + target + '">').find('#' + target).html(response);
      updateCard(target, $(APICache).find('#' + target).html());
      if ($(APICache).find('#' + target).length <= 0) {
        $(APICache).find('#' + target).append('<p>Ooops! Empty</p>');
      }
      if ($(APICache).find('#' + target + ' form').length > 0) {
        bindForm($('#' + target + ' form'));
      }
    };
  }

function bindForm (form) {
  $(form).submit(function(e) { 
    e.preventDefault();
    $.ajax({
      url   : form.attr('action'),
      type  : form.attr('method'),
      data  : form.serialize(), // data to be submitted
      success: function(response){
        var target = $(form).closest(".card");
        if ($(target).length > 0) {
          fillCard($(target).attr('id'), $(target).data('url'));
        }
        else if ($(target).length <= 0) {
          alert('no card');
        }
      }
    });
    return false;
  });
}
