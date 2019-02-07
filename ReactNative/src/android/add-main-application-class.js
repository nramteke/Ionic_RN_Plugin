module.exports = function (context) {
	console.log('Adding MainApplication class in mainfest file ...')
	var fs = require('fs');
	var path = require("path");
	var projectRoot = context.opts.projectRoot;
	console.log('=:Project Dir:=' + projectRoot)
	var configPath = path.join(projectRoot, 'platforms', 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
	console.log('=:File being modified:=' + configPath)

	//to replace the <application tag 
	var oldValue = '<application';
	var newValue = '<application android:name="cordova.plugin.reactnative.MainApplication" ';

	fs.readFile(configPath, 'utf8', function (err, data) {
		if (err) {
			return console.log(err);
		}
		//console.log('the value=='+data.indexOf('cordova.plugin.reactnative.MainApplication'));
		if (data.indexOf('cordova.plugin.reactnative.MainApplication') == -1) {
			var result = data.replace(oldValue, newValue);
			fs.writeFile(configPath, result, 'utf8', function (err) {
			if (err) return console.log(err);
			});
			console.log('=:File modified successfully:=')
		}else{
			console.log('already exist');
		}
	});
}