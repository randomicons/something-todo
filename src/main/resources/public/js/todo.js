$.fn.datepicker.setDefaults({
    format: 'm-d-yy',
    date: null,
    startDate: new Date()
});

$.fn.textWidth = function (text, font) {

    if (!$.fn.textWidth.fakeEl) $.fn.textWidth.fakeEl =
        $('<span>').css("white-space", "pre").hide().appendTo(document.body);

    $.fn.textWidth.fakeEl.text(text || this.val() || this.text() || this.attr('placeholder')).css('font', font || this.css('font'));
    return $.fn.textWidth.fakeEl.width();
};

var userId;
var focusedTask;
var dueDateMouseOver;

function onSignIn(authResult) {
    $("#todo-list").toggle()
    $.ajax({
        type: 'POST',
        url: '/',
        // Always include an `X-Requested-With` header in every AJAX request,
        // to protect against CSRF attacks.
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        },
        contentType: 'application/octet-stream; charset=utf-8',
        success: function (result) {
            userId = result.split("\n", 1)[0]
            $("main").html(result.substring(result.indexOf("\n") + 1))
            Intercooler.processNodes($("main"))
            setTodoListBindings()
        },
        processData: false,
        data: authResult.getAuthResponse().access_token + "\n" +
            authResult.getAuthResponse().id_token
    });
}

function blurOnSubmit() {
    focusedTask.blur()
    $(focusedTask).removeClass("focused")
    focusedTask = null
    console.log("blurred")
}

function setTodoListBindings() {

    var todolist = $("#todo-list");

    todolist.on("mouseover", "li", function () {
        $(this).find(".actions").css("visibility", "visible");
        if ($(this).find(".input-date").css("visibility") == "hidden") {
            dueDateMouseOver = $(this).find(".input-date");
            dueDateMouseOver.css("visibility", "visible");
        }
    });



    todolist.on("mouseleave", "li", function () {
        $(this).find(".actions").css("visibility", "hidden");
        if (dueDateMouseOver != null) {
            dueDateMouseOver.css("visibility", "hidden");
            dueDateMouseOver = null;
        }
    });

    todolist.on("focus", ".edit-task input", function () {
        $(this).addClass("focused");
        focusedTask = this
    });

    todolist.on("blur", ".edit-task input", function () {
        $(this).removeClass("focused");
    });

    todolist.on("elementAdded.ic", function () {
        $("[data-toggle='datepicker']").datepicker();
    });
    $("[data-toggle='datepicker']").datepicker();

    //Pomo button event handler
    $("#todo-list").on("click", ".button-pomo", function () {
        startPomo($(this).closest("li"));
    });

    //Dynamic Width Bindings

    $(".width-dynamic").each(function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    });
    $('#todo-list').on('input', '.width-dynamic', function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    }).trigger('input');

    $('#todo-list').on('elementAdded.ic', '.width-dynamic', function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    });

    $("#save_button").click(function () {
        save();
    })
}

function save() {
    console.log("trying to save")
    $.ajax({
        type: 'POST',
        url: 'save/' + userId,
        // Always include an `X-Requested-With` header in every AJAX request,
        // to protect against CSRF attacks.
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        },
        contentType: 'application/json',
        dataType: "json",
        success: function (result) {
            console.log("save sucesss" + result)
        },
        async: false,
        data: createSaveBody()
    });
}

function setBindings() {
    setTodoListBindings()
    $(window).on('unload', function () {
        save();
    });

}

function createSaveBody() {
    var out = ""
    $(".edit-task").each(function (index, e) {
        out += (index + 1) + ". " + $(e).find(".input-name").val();
        out += " date: " + $(e).find(".input-date input").val() + "\n"
    })
    return out;
}

$(document).ready(setBindings());
