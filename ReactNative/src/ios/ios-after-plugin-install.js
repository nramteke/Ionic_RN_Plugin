module.exports = function(context) {
	console.log('Replacing AppDelegate ...')
	var fs = context.requireCordovaModule('fs')
	var path = context.requireCordovaModule('path')
	var newAppDelegateHeader = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/ios/AppDelegate.h')
	var newAppDelegateSource = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/ios/AppDelegate.m')
	var oldAppDelegateHeader = path.join(context.opts.projectRoot,'platforms/ios/SCBLIFE/Classes/AppDelegate.h')
	var oldAppDelegateSource = path.join(context.opts.projectRoot,'platforms/ios/SCBLIFE/Classes/AppDelegate.m')
	fs.createReadStream(newAppDelegateHeader).pipe(fs.createWriteStream(oldAppDelegateHeader))
	fs.createReadStream(newAppDelegateSource).pipe(fs.createWriteStream(oldAppDelegateSource))
	console.log('Replacing AppDelegate done.')
	
	console.log('Replacing Bridging-Header.h ...')
	var newBridgingHeader = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/ios/Bridging-Header.h')
	var oldBridgingHeader = path.join(context.opts.projectRoot,'platforms/ios/SCBLIFE/Bridging-Header.h')
	fs.createReadStream(newBridgingHeader).pipe(fs.createWriteStream(oldBridgingHeader))
	console.log('Replacing Bridging-Header.h done.')
	
	console.log('Replacing RCTSRWebSocket.m ...')
	var newWebSocket = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/ios/RCTSRWebSocket.m')
	var oldWebSocket = path.join(context.opts.projectRoot,'node_modules/react-native/Libraries/WebSocket/RCTSRWebSocket.m')
	fs.createReadStream(newWebSocket).pipe(fs.createWriteStream(oldWebSocket))
	console.log('Replacing RCTSRWebSocket.m done.')
}