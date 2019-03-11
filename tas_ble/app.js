/**
 * Created by ryeubi on 2015-08-31.
 * Updated 2017.03.06
 * Made compatible with Thyme v1.7.2
 */

var net = require('net');
var util = require('util');
var fs = require('fs');
var xml2js = require('xml2js');
var Bleacon = require('bleacon');


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

var uuid1 = '24ddf4118cf1440c87cde368daf9c93e';
var uuid2 = '24ddf4118cf1440c87cde368daf9c001';
var uuid3 = '24ddf4118cf1440c87cde368daf9c93e';
var uuid4 = '24ddf4118cf1440c87cde368daf9c93e';
var uuid5=  '24ddf4118cf1440c87cde368daf9c93e';
var uuid6 = '24ddf4188cf1440c87cde368daf9c93e';
var uuid7 = '24ddf4118cf1440c87cde368daf9c93e';
var uuid8 = '24ddf4118cf1440c87cde368daf9c93e';
var uuid9 = '24ddf4118cf1440c87cde368daf9c93e';

// Beacon1 Major / Minor 값 // 5013080 (2) C1:49:4E:D0:F4:0D
var major1 = 501;
var minor1 = 3080;

// Beacon2 Major / Minor 값 // 50103092 (5-1)FE:7D:0C:27:ED:C1
var major2 = 33333;
var minor2 = 3;

// Beacon3 Major / Minor 값 // 50103063 (26) DC:1C:1D:35:97:FD
var major3 = 2;
var minor3 = 26;

// Beacon4 Major / Minor 값 // 50103071 (9) ED:8D:36:76:EE:97
var major4 = 501;
var minor4 = 3071;

//Beacon5 Major / Minor 값 // 50103073 (32) CD:12:10:44:6E:1D 
var major5=501;
var minor5=3073;

//Beacon6 / 50104491 (8) EBL97:2A:18:B1:A1
var major6=724; 
var minor6=2;

//Beacon7 Major / Minor 값 // 50103062 (35) F9:D7:20:B8:2B:10
var major7=501;
var minor7=3062;

//Beacon8 Major / Minor 값 // 50103084 (1) D7:93:ED:D3:31:BA
var major8=11111;
var minor8=1;

//Beacon9 Major / Minor 값 // 50103085 (24) C4:2F:7A:D3:B3:84
var major9=2;
var minor9=24;

var distance_1 = [];
var distance_2 = [];
var distance_3 = [];
var distance_4 = [];
var distance_5 = [];
var distance_6= [];
var distance_7 = [];
var distance_8 = [];
var distance_9 = [];

var d1 = 0;
var d2 = 0;
var d3 = 0;
var d4 = 0;
var d5 = 0;
var d6 = 0;
var d7 = 0;
var d8 = 0;
var d9 = 0;

var num_1 = 0;
var num_2 = 0;
var num_3 = 0;
var num_4 = 0;
var num_5 = 0;
var num_6 = 0;
var num_7 = 0;
var num_8 = 0;
var num_9 = 0;

Bleacon.startScanning();
Bleacon.on('discover', function(bleacon){
    if(bleacon) {
        if(d1 && d2 && d3 && d4 && d5 && d6 && d7 && d8 && d9){
            console.log(find_position(d1, d2, d3, d4, d5, d6, d7, d8, d9));
        }
    //console.log('bleacon');
        if((bleacon.uuid == uuid1) && (bleacon.major == major1) && (bleacon.minor == minor1)) {
            distance_1.push(parseFloat(bleacon.accuracy));
            //console.log("11111 : " + bleacon.accuracy);
            num_1 = num_1 + 1;
            if(num_1 == 5){
                distance_1.sort();
                var sum = 0;
                for(var i=0;i<distance_1.length;i++)
                {
                    sum += distance_1[i];
                }
                d1 = sum / (distance_1.length);
                console.log("bleacon1 aver. : " + d1);
                distance_1 = [];
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                //distance_4.push(2.1);
                num_1 = 0;
            }
        }
        else if((bleacon.uuid == uuid2) && (bleacon.major == major2) && (bleacon.minor == minor2)) {
            distance_2.push(parseFloat(bleacon.accuracy));
            num_2 = num_2 + 1;
            // console.log("222 : " + bleacon.accuracy);
            if(num_2 == 5){
                distance_2.sort();
                var sum = 0;
                for(var i=0;i<distance_2.length;i++)
                {
                    sum += distance_2[i];
                }
                d2 = sum / (distance_2.length);
                console.log("bleacon2 aver. : " + d2);
                distance_2 = [];
                //distance_1.push(2.1);
                //distance_3.push(2.1);
                //distance_4.push(2.1);
                num_2 = 0;
            }
        }
        else if((bleacon.uuid == uuid3) && (bleacon.major == major3) && (bleacon.minor == minor3)) {
            distance_3.push(parseFloat(bleacon.accuracy));
            num_3 = num_3 + 1;
            // console.log("33 : " + bleacon.accuracy);
            if(num_3 == 5){
                distance_3.sort();
                var sum = 0;
                for(var i=0;i<distance_3.length;i++)
                {
                    sum += distance_3[i];
                }
                d3 = sum / (distance_3.length);
                console.log("bleacon3 aver. : " + d3);
                distance_3 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_4.push(2.1);
                num_3 = 0;
            }
        }
        else if((bleacon.uuid == uuid4) && (bleacon.major == major4) && (bleacon.minor == minor4)) {
            distance_4.push(parseFloat(bleacon.accuracy));
            // console.log("444 : " + bleacon.accuracy);
            num_4 = num_4 + 1;
            if(num_4 == 5){
                distance_4.sort();
                var sum = 0;
                for(var i=0;i<distance_4.length;i++)
                {
                    sum += distance_4[i];
                }
                d4 = sum / (distance_4.length);
                console.log("bleacon4 aver. : " + d4);
                distance_4 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_4 = 0;
            }
        }
        else if((bleacon.uuid == uuid5) && (bleacon.major == major5) && (bleacon.minor == minor5)){
            distance_5.push(parseFloat(bleacon.accuracy));
            //console.log("444 : " + bleacon.accuracy);
            num_5 = num_5 + 1;
            if(num_5 == 5){
                distance_5.sort();
                var sum = 0;
                for(var i=0;i<distance_5.length;i++)
                {
                    sum += distance_5[i];
                }
                d5 = sum / (distance_5.length);
                console.log("bleacon5 aver. : " + d5);
                distance_5 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_5 = 0;
            }
        }
        else if((bleacon.uuid == uuid6) && (bleacon.major == major6) && (bleacon.minor ==minor6)){
            distance_6.push(parseFloat(bleacon.accuracy));
            // console.log("444 : " + bleacon.accuracy);
            num_6 = num_6 + 1;
            if(num_6 == 5){
                distance_6.sort();
                var sum = 0;
                for(var i=0;i<distance_6.length;i++)
                {
                    sum += distance_6[i];
                }
                d6 = sum / (distance_6.length);
                console.log("bleacon6 aver. : " + d6);
                distance_6 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_6 = 0;
            }
        }
        else if((bleacon.uuid == uuid7) && (bleacon.major == major7) && (bleacon.minor ==minor7)){
            distance_7.push(parseFloat(bleacon.accuracy));
            // console.log("444 : " + bleacon.accuracy);
            num_7 = num_7 + 1;
            if(num_7 == 5){
                distance_7.sort();
                var sum = 0;
                for(var i=0;i<distance_7.length;i++)
                {
                    sum += distance_7[i];
                }
                d7 = sum / (distance_7.length);
                console.log("bleacon7 aver. : " + d7);
                distance_7 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_7 = 0;
            }
        }
        else if((bleacon.uuid == uuid8) && (bleacon.major == major8) && (bleacon.minor == minor8)){
            distance_8.push(parseFloat(bleacon.accuracy));
            // console.log("444 : " + bleacon.accuracy);
            
            num_8 = num_8 + 1;
            if(num_8 == 5){
                distance_8.sort();
                var sum = 0;
                for(var i=0;i<distance_8.length;i++)
                {
                    sum += distance_8[i];
                }
                d8 = sum / (distance_8.length);
                console.log("bleacon8 aver. : " + d8);
                distance_8 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_8 = 0;
            }
        }
        else if((bleacon.uuid == uuid9) && (bleacon.major == major9) && (bleacon.minor == minor9)){
            distance_9.push(parseFloat(bleacon.accuracy));
            // console.log("444 : " + bleacon.accuracy);
            num_9 = num_9 + 1;
            if(num_9 == 5){
                distance_9.sort();
                var sum = 0;
                for(var i=0;i<distance_9.length;i++)
                {
                    sum += distance_9[i];
                }
                d9 = sum / (distance_9.length);
                console.log("bleacon9 aver. : " + d9);
                distance_9 = [];
                //distance_1.push(2.1);
                //distance_2.push(2.1);
                //distance_3.push(2.1);
                num_9 = 0;
            }
        }
        else {
        }

        var exe = timer_upload_action;
        exe();
    }
});

function find_position(di1, di2, di3, di4,di5, di6, di7, di8, di9 ,r){

    var now = Math.min(di1, di2, di3, di4,di5, di6, di7, di8, di9);
    var result;

    if(now == di1) now = "0";//"2번 비콘";
    else if(now == di2) now = "1";//"5-1번 비콘";
    else if(now == di3) now = "2";//"26번 비콘";
    else if(now == di4) now = "3";//"9번 비콘";
    else if(now == di5) now = "4";//"32번 비콘";
    else if(now == di6) now = "5";//"8번 비콘";
    else if(now == di7) now = "6";//"35번 비콘";
    else if(now ==di8) now ="7";//"1번 비콘";
    else if(now==di9) now="8";//"24번 비콘";

    else now = "파악 실패"

    result = "2번: " + di1 + "\n";
    result += "5-1번: " + di2 + "\n";
    result += "26번: " + di3 + "\n";
    result += "9번: " + di4 + "\n";
    result += "32번: " + di5 + "\n";
    result += "8번: " + di6 + "\n";
    result += "35번: " + di7 + "\n";
    result += "1번: " + di8 + "\n";
    result += "24번: " + di9 + "\n";
    
    result += "현재위치: " + now + "번 비콘";
    
   // return result;
   return now;
}

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

var net = require('net');

function timer_upload_action() {
    if (tas_state == 'upload') {
        var con = find_position(d1, d2, d3, d4, d5, d6, d7, d8, d9, 0.3);
        
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

// wdt.set_wdt(require('shortid').generate(), 2, timer_upload_action);
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

