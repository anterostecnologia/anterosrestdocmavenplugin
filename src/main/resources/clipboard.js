

function addBlockClipboard() {
    $('.listingblock').each(function() {
        listingblock = $(this);
        if(listingblock.attr('id'))
            createBlockClipboard(listingblock);
    });
    try{
    var clipboard = new Clipboard('.asciidoctorcopypaste');
    clipboard.on('success', function(e) {
        var element = e.trigger;
        element.innerHTML='&nbsp;&nbsp;&nbsp;Copied!';
        setTimeout(function () {
            element.innerHTML='&nbsp;';
        },1000);
        e.clearSelection();
    });
    }catch(e){console.log(e)}
}

function createBlockClipboard(block) {
    var id = block.attr('id');
    var str = '<button class="btn asciidoctorcopypaste" id="'+id+'-copy-button" data-clipboard-target="#'+id+'">&nbsp;</button>';
    $(str).insertBefore(block);
}

$(addBlockClipboard);