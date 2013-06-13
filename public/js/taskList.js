define(['jquery', 'sortable'], function ($) {

    $(document).ready(function($) {
        sortablize();
        var $tasks = $("#tasks");

    $tasks.find("#inventory li").click(function(e)
        {
      //moveTaskToList($(this), $('#today ol.taskList'));
    });
    });

    

  function sortablize(list) {
      if (!list) {
          list = $('.sortable');
      }
      $(list).sortable({
          placeholder: "sortable-placeholder",
          revert: "100",
          update: function (event, ui) {
                        var taskId = ui.item.data("taskid");
                        var newPriority = ui.item.index();
                        $.post("/tasks/reprioritizeTask", "taskId=" + taskId + "&newPriority=" + newPriority);
          }
      });
      $(list).find('.pomodoros li').each(function (index) {
          if ($(this).hasClass('broken')) {
              $(this).html('<img src="assets/img/icon_broken.svg" />');
          }
          else if ($(this).hasClass('interrupted')) {
              $(this).html('<img src="assets/img/icon_interrupted.svg" />');
          }
          else if ($(this).hasClass('active')) {
              $(this).html('<img src="assets/img/icon_inprogress.svg" />');
          }
      });
  }

  function moveTaskToList(task, list) {
      $(list).append($(task));
  }

    return {
        markAsActive: function (pomodoro) {
            if (!pomodoro) {
                pomodoro = $('#today').find('.task .pomodoros li.fresh').first();
            }
            $(pomodoro).addClass('inproactivegress').html('<img src="assets/img/icon_inprogress.svg" />');
        },
        markAsBroken: function(pomodoro) {
            if (! pomodoro) {
                pomodoro = $('#today').find('.task .pomodoros .active');
            };
            if (! $(pomodoro).hasClass('broken')) {
                $(pomodoro).removeClass('active active').addClass('broken').html('<img src="assets/img/icon_broken.svg" />');
            };
        },
        markAsInterrupted: function(pomodoro) {
            if (! pomodoro) {
                pomodoro = $('#today').find('.task .pomodoros .active');
            };
            if (! $(pomodoro).hasClass('interrupted')) {
                $(pomodoro).addClass('interrupted').html('<img src="assets/img/icon_interrupted.svg" />');
            };
        },
        markAsEnded: function(pomodoro) {
            if (! pomodoro) {
                pomodoro = $('#today').find('.task .pomodoros .active');
            };
            if (! $(pomodoro).hasClass('interrupted')) {
                $(pomodoro).addClass('interrupted').html('<img src="assets/img/icon_interrupted.svg" />');
            };
        },
        addTaskToList: function (targetList, title, numberOfPoms) {
            var $newLi = $('<li draggable="true" class="task new-task" data-taskId="TODO"/>');
            $newLi.append('<h3>'+title+'</h3>');
            $newLi.append('<ol class="pomodoros">');
            for (var i = numberOfPoms - 1; i >= 0; i--) {
                $newLi.find('.pomodoros').append('<li class="active">&nbsp;</li>');
            };
            $(targetList).append($newLi);
            setTimeout(function(){
                $(targetList).find(".new-task").removeClass('new-task');
            }, 250);
            $(targetList).sortable('refresh')
        }
    }
});