$(document).ready(function() {
    var defaultLang='CHANGEME';
    var languages = ['LANG1'];

    var toolbar='CHANGEME';
    var position='none';

    switch( position ){
        case 'toc':
            $(toolbar).insertAfter($('#toctitle'));
            break
        case 'header':
            $(toolbar).insertAfter($('#header h1'));
            break
    }

    function showLanguage(lang){
        for(var l in languages) {
            $('.language-'+languages[l]).hide();
        }
        $('.language-'+lang).show();
    }

    for(var l in languages) {
        $('.flag-'+languages[l]).click(function () {
            console.log($(this))
            showLanguage($(this).attr('alt'));
        });
        $('.sect1.language-'+languages[l]).each(function(){
            var sec = $(this).children(":first");
            var id = $(sec).attr('id');
            $('ul.sectlevel1  a[href$="#'+id+'"]').each(function(){
                var ul = $(this).parent();
                ul.addClass("language-"+languages[l]);
            });
        });
        $('.sect2.language-'+languages[l]).each(function(){
            var sec = $(this).children(":first");
            var id = $(sec).attr('id');
            $('ul.sectlevel2  a[href$="#'+id+'"]').each(function(){
                var ul = $(this).parent();
                ul.addClass("language-"+languages[l]);
            });
        });
        $('.sect3.language-'+languages[l]).each(function(){
            var sec = $(this).children(":first");
            var id = $(sec).attr('id');
            $('ul.sectlevel3  a[href$="#'+id+'"]').each(function(){
                var ul = $(this).parent();
                ul.addClass("language-"+languages[l]);
            });
        });
    }

    showLanguage(defaultLang);
});
