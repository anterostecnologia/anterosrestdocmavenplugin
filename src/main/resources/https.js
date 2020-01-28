$(document).ready(function() {
    if (location.protocol !== 'https:')
        if (location.hostname !== 'localhost')
            if (location.hostname !== '127.0.0.1')
                location.protocol = 'https:';
});