var exec = require('cordova/exec');

exports.startReact = function (screenID,userID,  height, success, error) {
    exec(success, error, 'ReactNative', 'startReact', [screenID,userID,height]);
};

exports.switchReact = function (screenID,userID,  height, success, error) {
    exec(success, error, 'ReactNative', 'switchReact', [screenID,userID,height]);
};

exports.endReact = function (success, error) {
    exec(success, error, 'ReactNative', 'endReact');
};
