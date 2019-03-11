/**
 * Created by ryeubi on 2015-08-31.
 * Updated 2017.03.06
 * Made compatible with Thyme v1.7.2
 */

var net = require('net');
var util = require('util');
var fs = require('fs');
var xml2js = require('xml2js');


var wdt = require('./wdt');
//var sh_serial = require('./serial');

var serialport = require('serialport');

var usecomport = '';
var usebaudrate = '';
var useparentport = '';
var useparenthostname = '';

var upload_arr = [];
var download_arr = [];

var conf = {};

// This is an async file read
fs.readFile('conf.xml', 'utf-8', function (err, data) {
    if (err) {
        console.log("FATAL An error occurred trying to read in the file: " + err);
        console.log("error : set to default for configuration")
    }
    else {
        var parser = new xml2js.Parser({explicitArray: false});
        parser.parseString(data, function (err, result) {
            if (err) {
                console.log("Parsing An error occurred trying to read in the file: " + err);
                console.log("error : set to default for configuration")
            }
            else {
                var jsonString = JSON.stringify(result);
                conf = JSON.parse(jsonString)['m2m:conf'];

                usecomport = conf.tas.comport;
                usebaudrate = conf.tas.baudrate;
                useparenthostname = conf.tas.parenthostname;
                useparentport = conf.tas.parentport;

                if(conf.upload != null) {
                    if (conf.upload['ctname'] != null) {
                        upload_arr[0] = conf.upload;
                    }
                    else {
                        upload_arr = conf.upload;
                    }
                }

                if(conf.download != null) {
                    if (conf.download['ctname'] != null) {
                        download_arr[0] = conf.download;
                    }
                    else {
                        download_arr = conf.download;
                    }
                }
            }
        });
    }
});


var tas_state = 'init';

var upload_client = null;

var t_count = 0;

var pir=100;

var SerialPort = require('serialport').SerialPort; 
var port = new SerialPort('/dev/ttyUSB0',{
  baudrate: 9600,
  //parser: SerialPort.parsers.readline('\n')
});
 
port.on('open', function() {
  port.write('1', function(err) {
    if (err) {
      return console.log('Error on write: ', err.message);
    }
    console.log('1 written');
  });
});
 
// open errors will be emitted as an error event
port.on('error', function(err) {
  console.log('Error: ', err.message);
})
 
port.on('data', function (data) {
  console.log('Read and Send Data : ' + data);
  console.log('New PIR connection');
  var exe = timer_upload_action;

        if(data ==22951132121){
            pir=0;
        }
        else if(data == 197138125121){
            pir=1;
        }
        else if(data == 149129123121){
            pir=2;
        }
        else if(data == 1019129121){
            pir=3;
        }
        else if(data == 5212134121){
            pir=4;
        }
        else if(data == 101222134121){
            pir=5;
        }
        else if(data == 133246129121){
            pir=6;
        }
        else if(data == 149141121121){
            pir=7;
        }
        else if(data == 21238122121){
            pir=8;
        }
        else if(data == 197146134121){
            pir=9;
        }
        else if(data == 149193121121){
            pir=10;
        }
        else if(data == 181200123121){
            pir=11;
        }
        else if(data == 197243129121){
            pir=12;
        }
        else if(data == 14911127121){
            pir=13;
        }
        else if(data == 213204131121){
            pir=14;
        }
        else if(data == 133170132121){
            pir=15;
        }
        else if(data == 85243126121){
            pir=16;
        }
        else if(data == 37105129121){
            pir=17;
        }
        else if(data == 117123121121){
            pir=18;
        }
        else if(data == 1813123121){
            pir=19;
        }
        else if(data == 181186110121){
            pir=20;
        }
        else if(data == 69206125121){
            pir=21;
        }
        else if(data == 85163131121){
            pir=22;
        }
        else if(data == 69144125121){
            pir=23;
        }
        console.log( "pir: " + pir + "\n");

    
        if(pir != 100) {
            exe();
        }
});

function timer_upload_action() {
    if (tas_state == 'upload') {
        var con = pir;
        if((t_count % 7) == 0) {
        for (var i = 0; i < upload_arr.length; i++) {
            if (upload_arr[i].id == 'timer') {
                var cin = {ctname: upload_arr[i].ctname, con: con};
                    console.log(JSON.stringify(cin) + ' ---->');
                    upload_client.write(JSON.stringify(cin) + '<EOF>');
                    break;
                }
            }
        }
    }
}

function serial_upload_action() {
    if (tas_state == 'upload') {
        var buf = new Buffer(4);
        buf[0] = 0x11;
        buf[1] = 0x01;
        buf[2] = 0x01;
        buf[3] = 0xED;
        myPort.write(buf);
    }
}

var tas_download_count = 0;

function on_receive(data) {
    if (tas_state == 'connect' || tas_state == 'reconnect' || tas_state == 'upload') {
        var data_arr = data.toString().split('<EOF>');
        if(data_arr.length >= 2) {
            for (var i = 0; i < data_arr.length - 1; i++) {
                var line = data_arr[i];
                var sink_str = util.format('%s', line.toString());
                var sink_obj = JSON.parse(sink_str);

                if (sink_obj.ctname == null || sink_obj.con == null) {
                    console.log('Received: data format mismatch');
                }
                else {
                    if (sink_obj.con == 'hello') {
                        console.log('Received: ' + line);

                        if (++tas_download_count >= download_arr.length) {
                            tas_state = 'upload';
                        }
                    }
                    else {
                        for (var j = 0; j < upload_arr.length; j++) {
                            if (upload_arr[j].ctname == sink_obj.ctname) {
                                console.log('ACK : ' + line + ' <----');
                                break;
                            }
                        }

                        for (j = 0; j < download_arr.length; j++) {
                            if (download_arr[j].ctname == sink_obj.ctname) {
                                g_down_buf = JSON.stringify({id: download_arr[i].id, con: sink_obj.con});
                                console.log(g_down_buf + ' <----');
                                myPort.write(g_down_buf);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}


//var SerialPort = null;
var myPort = null;
function tas_watchdog() {
    if(tas_state == 'init') {
        upload_client = new net.Socket();

        upload_client.on('data', on_receive);

        upload_client.on('error', function(err) {
            console.log(err);
            tas_state = 'reconnect';
        });

        upload_client.on('close', function() {
            console.log('Connection closed');
            upload_client.destroy();
            tas_state = 'reconnect';
        });

        if(upload_client) {
            console.log('tas init ok');
            tas_state = 'init_serial';
        }
    }
    else if(tas_state == 'init_serial') {
    	SerialPort = serialport.SerialPort;
    	
        serialport.list(function (err, ports) {
            ports.forEach(function (port) {
                console.log(port.comName);
            });
        });

        myPort = new SerialPort(usecomport, {
            baudRate : parseInt(usebaudrate, 10),
            buffersize : 1
            //parser : serialport.parsers.readline("\r\n")
        });

        myPort.on('open', showPortOpen);
        myPort.on('data', saveLastestData);
        myPort.on('close', showPortClose);
        myPort.on('error', showError);

        if(myPort) {
            console.log('tas init serial ok');
            tas_state = 'connect';
        }
    }
    else if(tas_state == 'connect' || tas_state == 'reconnect') {
        upload_client.connect(useparentport, useparenthostname, function() {
            console.log('upload Connected');
            tas_download_count = 0;
            for (var i = 0; i < download_arr.length; i++) {
                console.log('download Connected - ' + download_arr[i].ctname + ' hello');
                var cin = {ctname: download_arr[i].ctname, con: 'hello'};
                upload_client.write(JSON.stringify(cin) + '<EOF>');
            }

            if (tas_download_count >= download_arr.length) {
                tas_state = 'upload';
            }
        });
    }
}

//wdt.set_wdt(require('shortid').generate(), 2, timer_upload_action);
wdt.set_wdt(require('shortid').generate(), 3, tas_watchdog);
//wdt.set_wdt(require('shortid').generate(), 3, serial_upload_action);

var cur_c = '';
var pre_c = '';
var g_sink_buf = '';
var g_sink_ready = [];
var g_sink_buf_start = 0;
var g_sink_buf_index = 0;
var g_down_buf = '';

function showPortOpen() {
    console.log('port open. Data rate: ' + myPort.options.baudRate);
}

var count = 0;
function saveLastestData(data) {
    var val = data.readUInt16LE(0, true);

    if(g_sink_buf_start == 0) {
        if(val == 0x16) {
            count = 1;
            g_sink_buf_start = 1;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 1) {
        if(val == 0x05) {
            count = 2;
            g_sink_buf_start = 2;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 2) {
        if(val == 0x01) {
            count = 3;
            g_sink_buf_start = 3;
            g_sink_ready.push(val);
        }
    }
    else if(g_sink_buf_start == 3) {
        count++;
        g_sink_ready.push(val);

        if(count >= 9){
            console.log(g_sink_ready);

            g_sink_ready = [];
            count = 0;
            g_sink_buf_start = 0;
        }
    }
}

function showPortClose() {
    console.log('port closed.');
}

function showError(error) {
    var error_str = util.format("%s", error);
    console.log(error.message);
    if (error_str.substring(0, 14) == "Error: Opening") {

    }
    else {
        console.log('SerialPort port error : ' + error);
    }
}

