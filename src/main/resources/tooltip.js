$(document).ready(function() {
    $('.listingblock').each( function() {
        var listingblock = $(this);
        var colist= listingblock.next();
        if( !colist.hasClass('colist') ) return;
        var texts = colist.find('p');
        $('.conum', listingblock).each(function(index){
            var conum = $(this);
            var txt = texts.length > index ? $(texts[index]).html() : '';
            conum.addClass('tooltip').append('<span class="tooltiptext">'+txt+'</span>')
        });
    });
});
