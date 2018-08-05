$.fn.textWidth = function (text, font) {

    if (!$.fn.textWidth.fakeEl) $.fn.textWidth.fakeEl = $('<span>').hide().appendTo(document.body);

    $.fn.textWidth.fakeEl.text(text || this.val() || this.text() || this.attr('placeholder')).css('font', font || this.css('font'));

    return $.fn.textWidth.fakeEl.width();
};

$(document).ready(function () {

    $(".width-dynamic").each(function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    });
    $('#todo-list').on('input', '.width-dynamic', function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    }).trigger('input');

    $('#todo-list').on('elementAdded.ic', '.width-dynamic', function () {
        $(this).css("width", $($(this).textWidth()).toEm());
    });
})