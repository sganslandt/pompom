define(['jquery', 'sortable', 'touch-punch'], function ($) {
  var ListTypes = {
    'today' : 'TodoToday', 
    'inventory': 'ActivityInventory'
  };

    $(document).ready(function($) {
        sortablize();

        $("form.createTaskForm").droppable({
          accept: "ol.taskList > li",
          drop: function( event, ui ) {
            if (ui.draggable.closest('section').attr('id') == 'inventory') {
              var targetList = '#today > ol.taskList'
            }
            else{
              var targetList = '#inventory > ol.taskList'
            };
            $(ui.draggable[0]).data('moveTo', targetList);
          }
        });
    });

    

  function sortablize(list) {
      if (!list) {
          list = $('.sortable');
      }
      $(list).sortable({
          placeholder: "sortable-placeholder",
          axis: "y",
          cursor: "move",
          handle: "ol.pomodoros",
          update: function (event, ui) {
            var taskId = ui.item.data("taskid");
            var newPriority = ui.item.index();
            $.post("/tasks/reprioritizeTask", "taskId=" + taskId + "&newPriority=" + newPriority);
          },
          start: function (event, ui) {
            ui.item.closest('section').addClass('dragging');
          },
          stop: function (event, ui) {
            if (ui.item.data('moveTo'))
            {
              list.sortable('cancel');
              moveTaskToList(ui.item, ui.item.data('moveTo'))
              ui.item.data('moveTo', '')
            };
            list.closest('section').removeClass('dragging');
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

  function moveTaskToList(task, targetList) {
    var listType = ListTypes[$(targetList).closest('section').attr('id')];
    var taskId = $(task[0]).data("taskid");
    $.post("/tasks/moveTaskToList", "taskId=" + taskId + "&newList=" + listType);
    $(targetList).append($(task[0]));
    privateRefreshList(targetList);
  }
  function privateRefreshList (targetList) {
    if (! targetList) {
        targetList = $('ol.sortable');
    };
    $(targetList).sortable('refresh');
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
                $newLi.find('.pomodoros').append('<li class="fresh">&nbsp;</li>');
            };
            $(targetList).append($newLi);
            privateRefreshList(targetList);
        },
        refreshList: function (targetList) {
            privateRefreshList(targetList);
        }
    }
});