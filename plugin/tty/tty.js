 // Stores all terminal references, added as their slides come into view, by the state name.
var initialisedTerminals = {};

const RevealTty = {
    id: "tty",
    init: (reveal) => {
        Terminal.applyAddon(attach);
        Terminal.applyAddon(fit);
        var terminalSections = document.querySelectorAll('section[data-state^="terminal"]');
        terminalSections.forEach(function(section) {
            var slideId = section.getAttribute('data-state');
            var initTerminal = function() {
        
                var terminalElement = section.querySelector('*[data-is-terminal]');
        
                if (!terminalElement) {
                    return;
                }
                
                var port = terminalElement.getAttribute('port') || '8001';
                var endpoint = `localhost:${port}`;
                var terminalUrl = `//${endpoint}/terminal`;
        
                var dir = terminalElement.getAttribute('dir') || '';
                var command = terminalElement.getAttribute('command') || '';
                var updateTerminal = function(dir, command) {
                    console.log(location.pathname)
                    return fetch(terminalUrl, {
                        method: 'POST',
                        headers: {
                            "Content-Type": "application/json; charset=utf-8"
                        },
                        body: JSON.stringify({dir: dir, command: command, location: location.pathname})
                    });
                }
                if (initialisedTerminals[slideId]) {
                    updateTerminal(dir, command);
                    return;
                }
        
                var rows = terminalElement.getAttribute('rows') || 20;
                var cols = terminalElement.getAttribute('cols') || 80;
                var fontSize = terminalElement.getAttribute('font-size') || 20;
                var letterSpacing = terminalElement.getAttribute('letter-spacing');

                var terminal = initialisedTerminals[slideId] = new Terminal({
                    'macOptionIsMeta': true,
                    // TreeHouse colors from https://iterm2colorschemes.com/
                    'theme': {
                        'background': '#212121',
                        'foreground': '#8a7d65',
                        'cursor': '#fbd015',
                        'cursorAccent': '#212121',
                        'selection': '#8a7d65',
                    },
                    // These can be adjusted for the presentation
                    'rows': rows,
                    'cols': cols,
                    'fontSize': fontSize,
                    'letterSpacing': letterSpacing
                });
        
                terminal.on('resize', function(size) {
                    var cols = size.cols,
                        rows = size.rows,
                        url = terminalUrl + '/size?cols=' + cols + '&rows=' + rows;
                    fetch(url, {method: 'POST'});
                });
        
                terminal.open(terminalElement);
                terminal.fit();
                Reveal.layout();
                updateTerminal(dir, command)
                .then(function () {
                    var socket = new WebSocket(`ws://${terminalUrl}`);
                    socket.onopen = function() {
                        terminal.attach(socket);
                        terminal._initialized = true;
                        terminal.fit();
                        Reveal.layout();
                    };
                });
            };
            var setListener = function() {
                Reveal.addEventListener(slideId, function() {
                    setTimeout(initTerminal, 200); // Without the timeout the terminal is not loading (beats me!)
                }, false);
            };
        
            if (Reveal.getCurrentSlide() === section) {
                initTerminal();
            }
            setListener()
        });
    }
  };