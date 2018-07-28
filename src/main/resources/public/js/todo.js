$(document).ready(function () {
    

    $("#todo-list li .view").hover(function () {
        $(this).find("button").css("visibility", "visible");
    },
    function () {
        $(this).find("button").css("visibility", "hidden");
    });


});