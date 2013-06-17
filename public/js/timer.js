define('timer', ['jquery', 'taskList'], function ($, taskList) {
    var startTime = 0;
    var duration = 0;
    var pomodoroTimer = 0;
    var defaultDuration = 25;

    $(document).ready(function ($) {
        setupTimer();
        $('button#startPomodoro').click(function () {
            startTimer(defaultDuration);
        });
        $('button#breakPomodoro').click(function () {
            if (confirm('Do you really want to break this pomodoro')) {
                breakPomodoro();
            }
        });
        $('button#interruptPomodoro').click(function () {
            interruptPomodoro();
            showNotification();
        });
        $(document).on("taskList.Reprioritize", function(event) {
            if ($(event.list).closest('section').attr('id') == 'today')
            {
                setupTimer()
            };
        });
    });

    function setupTimer () {
        var $currentTask = $('#today').find('.taskList li').first();
        var $timer = $('#timer');
        $timer.find('#currentTaskTitle').html($currentTask.find('h3').html());
        $timer.find('#currentTaskPomodoros').html($currentTask.find('.pomodoros').html());
        var $activePomodoro = $timer.find('li.active').first();
        if ($activePomodoro.length >= 1)
        {
            startTime = $activePomodoro.data('starttime');
            duration = defaultDuration * 60;
            checkAndRestartPomodoroTimer();
        }
    }
    function startTimer(durationInMinutes) {
        startTime = $.now();
        duration = durationInMinutes * 60;
        updateTimeGrade(duration, 0);
        pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 1000);
        var $today = $('#today');
        if ($today.find('.taskList li').length < 1) {
            taskList.addTaskToList($today.find('.taskList'), 'Check out http://www.pomodorotechnique.com/', 1);
        }
        $today.find('.taskList li').first().append('<div class="timeStripe" />');
        taskList.markAsActive();
    }

    function checkAndRestartPomodoroTimer() {
        var now = new Date();
        var elapsedSeconds = (now - startTime) / 1000;
        if (elapsedSeconds < duration) {
            updateTimeGrade(duration, elapsedSeconds);
            pomodoroTimer = setTimeout(checkAndRestartPomodoroTimer, 200);
        }
        else {
            stopTimer();
        }
    }

    function updateTimeGrade(duration, elapsedTime) {
        var margin = ((duration-elapsedTime)*0.01666666666666667)+0.1;
        $('.timegradeholder').css('margin-left', '-'+margin+'rem');
        //var percentage = (elapsedTime / duration) * 100;
        //$('.timeStripe').width(percentage + '%')
    }

    function stopTimer() {
        var $activePomodoro = $('#timer').find('li.active').first();
        $activePomodoro.removeClass('active').addClass('ended');
        var now = new Date();
        $activePomodoro.data('endtime', now);
        clearTimeout(pomodoroTimer);
        updateTimeGrade(1, 1);
        document.getElementById('alarm').play();
    }

    function breakPomodoro() {
        stopTimer();
        taskList.markAsBroken();
    }

    function interruptPomodoro() {
        taskList.markAsInterrupted();
    }

    return{

    }
});