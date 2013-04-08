var httpRequest;
var APIurl = 'http://app.pompom.nu:9000/api';
var pomodoroTimer = 0;
var userLinkId = "currentUser";
var APICache = $('<div id="API"></div>');
var activeRequests = 0;
(function() {

	$(document).ready(function() {
    fetchApiAndBuild ();
	});

  $(document).on("ajaxDone",  function() {
    hideSplash();
  });
  $(document).on("requestAjaxRequestForMain", function(event, url, target){
    ajaxRequestForMain(url, target);
  });

  $(document).ajaxError(function(event, request, settings) {
    activeRequests--;
    console.log( "Error requesting " + settings.url );
    console.log(request.status + ' ' + request.statusText);
  });

  function fetchApiAndBuild () {
    activeRequests++;
    $.ajax({
      url: APIurl,
      dataType: 'html',
      success: function(response) {
        activeRequests--;
        var links = $(response).find('a');
        for (var i = 0; i < links.length; i++) {
          if ($(links[i]).attr("id") == userLinkId) {
            $(document).trigger("newCard", [$(links[i])]);
            $('#userId').attr('id', userLinkId + 'Link');
          }
          else {

            $(document).trigger("newCard", [$(links[i])]);
            insertInMenu($(links[i]));
          };
        }
        appBuilt();
      }
    });
  }

  function appBuilt () {
    if (activeRequests <= 0) {
      $(document).trigger("ajaxDone", ['appBuilt'])
    }
    else{
      recheckAppBuilt = setTimeout(appBuilt, 100);
    };
  }

  function hideSplash () {
    $('#spalshScreen').fadeOut(500)
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
    activeRequests++;
    $.ajax({
      url: url,
      dataType: 'html',
      success: function(response) {
        activeRequests--;
        var tempElement = $('<div>');
        $(tempElement).html(response);
        if ($(tempElement).find('.loginForm').length > 0) {
          location.reload();
        }
        else{
          var stripped = $(tempElement).find('main').html();
          updateAPICache(target, stripped)
        };
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
      // Nothing happens
    }
    else if ($(APICache).find('#' + target).length > 0) {
      $(APICache).find('#' + target).html(response);
      $('.card').trigger("cardUpdated", [target, $(APICache).find('#' + target).html()])
    }
    else{
      $(APICache).append('<div id="' + target + '">').find('#' + target).html(response);
      if ($(APICache).find('#' + target).length <= 0) {
        $(APICache).find('#' + target).append('<p>Ooops! Empty</p>');
      }
      $('.card').trigger("cardUpdated", [target, $(APICache).find('#' + target).html()])
    };
  }

}());