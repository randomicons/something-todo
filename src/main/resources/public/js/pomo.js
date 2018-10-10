const POMO_DURATION = 25 * 60;
// const POMO_DURATION = 10;

var pomoComplete = 0;
var curTaskTime = 0;
var curTask = null;
var timerRunning = false;
$(document).ready(function () {
});

function postPomo(curTask) {
    var path = curTask.find("form").attr("ic-put-to") + "/pomo/" + curTaskTime;
    $.post(path);
}

function startPomo(task) {
    if (curTask != null) {
        postPomo(curTask);
        curTaskTime = 0;
        curTask.css("background", "inherit");
        curTask.find(".pomo-countdown").html("");
    }
    curTask = task;
    if (!timerRunning) {
        var interval = setInterval(function () {
            if (pomoComplete == POMO_DURATION) {
                pomoComplete = 0;
                timerRunning = false;
                clearInterval(interval);
                // curTask.css("background", "inherit")
                postPomo(curTask);
                return;
            }
            curTaskTime += 1;
            timerRunning = true;
            pomoComplete += 1;
            percentDone = 100 * (pomoComplete / POMO_DURATION);

            curTask.css("background", "linear-gradient(90deg, #e3f8f8 " + percentDone + "%, #fce4e8 " + percentDone + "%)");
            var minleft = Math.floor((POMO_DURATION - pomoComplete) / 60);
            var secleft = (POMO_DURATION - pomoComplete) % 60;
            if (secleft < 10) secleft = "0" + secleft;
            curTask.find(".pomo-countdown").html("" + minleft + ":" + secleft);
        }, 1000)
    }

    percentDone = 100 * (pomoComplete / POMO_DURATION);
    curTask.css("background", "linear-gradient(90deg, #e3f8f8 " + percentDone + "%, #fce4e8 " + percentDone + "%)");
}