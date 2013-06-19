define('timer', ['jquery', 'taskList', 'modal'], function ($, taskList, modal) {
    var startTime = 0;
    var duration = 0;
    var pomodoroTimer = 0;
    var defaultDuration = 25;
    var $currentTask = {};
    var $timer = $('#timer');
    var $activePomodoro = {};

    $(document).ready(function ($) {
        setupTimer();
        $('button#startPomodoro').click(function () {
            startTimer(defaultDuration);
        });
        $('button#breakPomodoro').click(function () {
            openNoteModal('break')
        });
        $('button#interruptPomodoro').click(function () {
            openNoteModal('interrupt')
        });
        $(document).on("taskList.Reprioritize", function(event) {
            if ($(event.list).closest('section').attr('id') == 'today')
            {
                setupTimer()
            };
        });
    });

    function setupTimer () {
        updateCurrentTask();
        populateTimer();
        if ($activePomodoro.length >= 1)
        {
            startTime = Date.parse($activePomodoro.data('starttime'));
            duration = defaultDuration * 60;
            $timer.addClass('running')
            $(".sortable").sortable( "disable" );
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
        $.post("/tasks/" + $currentTask.data('taskid') + "/startPomodoro");
        taskList.markAsActive();
        $timer.addClass('running')
        $(".sortable").sortable( "disable" );
        populateTimer();
    }
    function updateCurrentTask () {
        $currentTask = $('#today').find('.taskList li').first();
    }
    function populateTimer()
    {
        $activePomodoro = $currentTask.find('.pomodoros li.active').first();
        $timer.find('#currentTaskTitle').html($currentTask.find('h3').html());
        $timer.find('#currentTaskPomodoros').html($currentTask.find('.pomodoros').html());
    }
    function checkAndRestartPomodoroTimer() {
        var now = $.now();
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
        $activePomodoro.removeClass('active').addClass('finished');
        $activePomodoro.data('endtime', $.now());
        clearTimeout(pomodoroTimer);
        updateTimeGrade(1, 1);
        document.getElementById('alarm').play();
        $timer.removeClass('running')
        $(".sortable").sortable( "enable" );
    }

    function breakPomodoro() {
        stopTimer();
        taskList.markAsBroken();
    }

    function interruptPomodoro() {
        taskList.markAsInterrupted();
    }
    function openNoteModal(type)
    {
        switch (type) {
            case 'break':       var title = 'Break pomodoro';
                                var content =  "<p>Why did you break this pomodoro</p> \
                                                <form class='breakPomodoroForm' name='breakPomodoroForm' method='post' action='/tasks/" + $currentTask.attr('taskid') + "/breakPomodoro'> \
                                                <textarea class='note' name='note' required='' placeholder='Why did you break this pomodoro'> </textarea>\
                                                <input name='breakPomodoroButton' type='submit' id='breakPomodoroButton' value='Submit'> \
                                                </form>";
                                break;
            case 'interrupt':   var title = 'Interrupt pomodoro';
                                var content =  "<p>Why did you interrupt this pomodoro</p> \
                                                <form class='interruptPomodoroForm' name='interruptPomodoroForm' method='post' action='/tasks/" + $currentTask.attr('taskid') + "/interruptPomodoro'> \
                                                <textarea class='note' name='note' required='' placeholder='Why did you interrupt this pomodoro'> </textarea>\
                                                <input name='interruptPomodoroButton' type='submit' id='interruptPomodoroButton' value='Submit'> \
                                                </form>";
                                break;
        };

        modal.new(title, content);

        $(".modal form").submit(function (eventData) {
            $.post(eventData.currentTarget.action, $(eventData.currentTarget).serialize(), function(data)
            {
                switch (type) {
                    case 'break':       breakPomodoro();
                                        break;

                    case 'interrupt':   interruptPomodoro()
                                        break;
                };
            });
            modal.destroy();
            return false;
        });

    }
});