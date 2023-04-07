var express = require('express');
var bodyParser = require('body-parser')
var serveStatic = require('serve-static');
var fs = require('fs');
var pty = require('node-pty');
var cors = require('cors');
var compression = require('compression')

var entrypoint = process.platform === 'win32' ? 'cmd.exe' : process.env.SHELL;

var terminal = pty.spawn(entrypoint, args = [], {
    name: 'xterm-color',
    scrollback: 9,
    cols: 80,
    rows: 24,
    cwd: process.env.PWD,
    env: process.env
});
var log = '';
var currentDir = process.env.HOME;

terminal.on('data', function(data) {
    log += data;
});

var app = express()
app.use(cors({
    credentials: true,
  }));
app.options('*', cors());

app.use(compression({ filter: shouldCompress }))

function shouldCompress (req, res) {
  if (req.headers['x-no-compression']) {
    // don't compress responses with this request header
    return false
  }
  // fallback to standard filter function
  return compression.filter(req, res)
}

require('express-ws')(app); // Support for websockets
app.use( bodyParser.json()); // Support parsing of POST data
app.use(bodyParser.urlencoded({
    extended: true
  }))

// Serve all content from the root.
app.use(serveStatic(__dirname, {'cacheControl': false}));

app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});

var cleanup = function() {
    terminal.write('\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b');
    terminal.write('\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b');
    terminal.write('\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b');
    terminal.write('\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b');

}

var changeDir = function(req) {
    if (!req.body.dir) {
        return;
    }

    if (currentDir == req.body.dir) {
        return;
    }
    currentDir = req.body.dir;
    cleanup();
    terminal.write("cd ${currentDir}\r");
    terminal.write(`clear\r`);
};

var command = function(req) {
    if (!req.body.command) {
        return;
    }
    cleanup();
    terminal.write(req.body.command);
};


app.post('/terminal', function(req, res) {
    console.log(req.body)
    changeDir(req);
    command(req);
    res.end();
    return;
});


app.post('/terminal/size', function (req, res) {
    if (!terminal) {
        res.end();
        return;
    }
    var cols = parseInt(req.query.cols),
        rows = parseInt(req.query.rows);

    terminal.resize(cols, rows);
    res.end();
});

app.ws('/terminal', function (ws) {
    ws.send(log);
    var wsTerminal = terminal;
    terminal.on('data', function(data) {
        try {
            ws.send(data);
        } catch (e) {
            // console.error(e);
        }
    });

    ws.on('message', function(message) {
        terminal.write(message);
    });

    ws.on('close', function() {
        return; // Do nothing
    })
});

var port = process.env.PORT || 8000,
    host = '0.0.0.0';

console.log('App listening to http://' + host + ':' + port);
app.listen(port, host);
