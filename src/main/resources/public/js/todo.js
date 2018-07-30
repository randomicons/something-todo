$(document).ready(function () {

    $("#todo-list").on("mouseover", "li", function () {

        $(this).find("button").css("visibility", "visible");
    })

    $("#todo-list").on("mouseleave", "li", function () {

        $(this).find("button").css("visibility", "hidden");
    })

    $("#todo-list").on("focus", "input", function () {
        $(this).closest("li").addClass("focused");
    })

    $("#todo-list").on("blur", "input", function () {
        $(this).closest("li").removeClass("focused");
    })
});

