define('timer', ['jquery', 'taskList', 'modal', 'favicon'], function ($, taskList, modal, favicon) {
    "use strict";
    var startTime = 0,
        duration = 0,
        pomodoroTimer = 0,
        defaultDuration = 25,
        $currentTask = {},
        $timer = $('#timer'),
        $activePomodoro = {};

    $(document).ready(function ($) {
        setupTimer();
        $('button#startPomodoro').click(function () {
            startTimer(defaultDuration);
        });
        $('button#breakPomodoro').click(function () {
            openNoteModal('break');
        });
        $('button#interruptPomodoro').click(function () {
            openNoteModal('interrupt');
        });
        $(document).on("taskList.Reprioritize", function (event) {
            if ($(event.list).closest('section').attr('id') === 'today') {
                setupTimer();
            }
        });
    });

    function setupTimer() {
        updateCurrentTask();
        populateTimer();
        if ($activePomodoro.length >= 1) {
            startTime = Date.parse($activePomodoro.data('starttime'));
            duration = defaultDuration * 60;
            $timer.addClass('running');
            $(".sortable").sortable("disable");
            checkAndRestartPomodoroTimer();
        }
    }

    function startTimer(durationInMinutes) {
        startTime = $.now();
        duration = durationInMinutes * 60;
        updateTimeGrade(duration, 0);
        updateFavicon(durationInMinutes);
        pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 1000);
        var $today = $('#today');
        if ($today.find('.taskList li').length < 1) {
            taskList.addTaskToList($today.find('.taskList'), 'Check out http://www.pomodorotechnique.com/', 1);
        }
        $.post("/tasks/" + $currentTask.data('taskid') + "/startPomodoro");
        taskList.markAsActive();
        $timer.addClass('running');
        $(".sortable").sortable("disable");
        populateTimer();
    }

    function updateCurrentTask() {
        $currentTask = $('#today').find('.taskList li').first();
    }

    function populateTimer() {
        $activePomodoro = $currentTask.find('.pomodoros li.active').first();
        $timer.find('#currentTaskTitle').html($currentTask.find('h3').html());
        $timer.find('#currentTaskPomodoros').html($currentTask.find('.pomodoros').html());
    }

    function checkAndRestartPomodoroTimer() {
        var now = $.now(),
            elapsedSeconds = (now - startTime) / 1000,
            minutesRemaining = Math.floor(duration / 60) - Math.floor(elapsedSeconds / 60);
        if (elapsedSeconds < duration) {
            updateTimeGrade(duration, elapsedSeconds);
            updateFavicon(minutesRemaining);
            pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 200);
        } else {
            stopTimer();
        }
    }

    function updateTimeGrade(duration, elapsedTime) {
        var margin = ((duration - elapsedTime) * 0.01666666666666667) + 0.1;
        $('.timegradeholder').css('margin-left', '-' + margin + 'rem');
        //var percentage = (elapsedTime / duration) * 100;
        //$('.timeStripe').width(percentage + '%')
    }

    function stopTimer() {
        $activePomodoro.removeClass('active').addClass('finished');
        $activePomodoro.data('endtime', $.now());
        clearTimeout(pomodoroTimer);
        updateTimeGrade(1, 1);
        resetFavicon();
        document.getElementById('alarm').play();
        $timer.removeClass('running');
        $(".sortable").sortable("enable");
    }

    function breakPomodoro() {
        stopTimer();
        taskList.markAsBroken();
    }

    function interruptPomodoro() {
        taskList.markAsInterrupted();
    }

    function updateFavicon(minutes) {
        var xOffset = 2 + Math.floor(minutes / 10);
        $.favicon('assets/img/icon/16-badge.png', function (ctx) {
            ctx.font = 'bold 8px "helvetica", "arial", sans-serif';
            ctx.fillStyle = '#6a2b1a';
            ctx.fillText(minutes, xOffset, 8);
        });
    }

    function resetFavicon() {
        $.favicon('assets/img/icon/favicon.ico');
    }

    function openNoteModal(type) {
        var title,
            content;
        switch (type) {
        case 'break':
            title = 'Break pomodoro';
            content = "<p>Why did you break this pomodoro</p> \
                       <form class='breakPomodoroForm' name='breakPomodoroForm' method='post' action='/tasks/" + $currentTask.data('taskid') + "/breakPomodoro'> \
                           <textarea class='note' name='note' required='' placeholder='Why did you break this pomodoro'> </textarea>\
                           <input name='breakPomodoroButton' type='submit' id='breakPomodoroButton' value='Submit'> \
                       </form>";
            break;
        case 'interrupt':
            title = 'Interrupt pomodoro';
            content = "<p>Why did you interrupt this pomodoro</p> \
                       <form class='interruptPomodoroForm' name='interruptPomodoroForm' method='post' action='/tasks/" + $currentTask.data('taskid') + "/interruptPomodoro'> \
                           <textarea class='note' name='note' required='' placeholder='Why did you interrupt this pomodoro'> </textarea>\
                           <input name='interruptPomodoroButton' type='submit' id='interruptPomodoroButton' value='Submit'> \
                       </form>";
            break;
        }
        modal.create(title, content);
        $(".modal form").submit(function (eventData) {
            $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize(), function () {
                switch (type) {
                case 'break':
                    breakPomodoro();
                    break;

                case 'interrupt':
                    interruptPomodoro();
                    break;
                }
            });
            modal.destroy();
            return false;
        });

    }
});