$(document).ready(function() {

    $('.sectlevel1 > li').addClass('plusimageapply');
    $('.sectlevel1 > li').children().addClass('selectedimage');
    $('.sectlevel1 > li > ul').children().hide();
    $('.sectlevel1 > li').each( function(column) {
        $(this).click(function(event){
            if (this == event.target.parentNode) {
                if($(this).is('.plusimageapply')) {
                    $('ul:first',$(this)).children().each(function(){$(this).show()});
                    $(this).removeClass('plusimageapply'); $(this).addClass('minusimageapply');
                } else {
                    $('ul:first',$(this)).children().each(function(){$(this).hide()});
                    $(this).removeClass('minusimageapply'); $(this).addClass('plusimageapply');
                }
            }
        });
    } );

    $('.sectlevel2 > li').addClass('plusimageapply');
    $('.sectlevel2 > li').children().addClass('selectedimage');
    $('.sectlevel2 > li > ul').children().hide();
    $('.sectlevel2 > li').each( function(column) {
        $(this).click(function(event){
            if (this == event.target.parentNode) {
                if($(this).is('.plusimageapply')) {
                    $('ul',$(this)).children().each(function(){$(this).show()});
                    $(this).removeClass('plusimageapply'); $(this).addClass('minusimageapply');
                } else {
                    $('ul',$(this)).children().each(function(){$(this).hide()});
                    $(this).removeClass('minusimageapply'); $(this).addClass('plusimageapply');
                }
            }
        });
    } );

    $('.sectlevel3 > li').addClass('plusimageapply');
    $('.sectlevel3 > li').children().addClass('selectedimage');
    $('.sectlevel3 > li > ul').children().hide();
    $('.sectlevel3 > li').each( function(column) {
        $(this).click(function(event){
            if (this == event.target.parentNode) {
                if($(this).is('.plusimageapply')) {
                    $('ul',$(this)).children().each(function(){$(this).show()});
                    $(this).removeClass('plusimageapply'); $(this).addClass('minusimageapply');
                } else {
                    $('ul',$(this)).children().each(function(){$(this).hide()});
                    $(this).removeClass('minusimageapply'); $(this).addClass('plusimageapply');
                }
            }
        });
    } );

});
