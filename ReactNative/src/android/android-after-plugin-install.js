module.exports = function(context) {
	console.log('Replacing MainActivity.java and build.gradle and copying gradle.properties ...')

	var fs = context.requireCordovaModule('fs')
	var path = context.requireCordovaModule('path')
	var newMainActivity = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/android/MainActivity.java')
	var oldMainActivity = path.join(context.opts.projectRoot,'platforms/android/app/src/main/java/th/co/scblife/easy/MainActivity.java')
	fs.createReadStream(newMainActivity).pipe(fs.createWriteStream(oldMainActivity))

	var sourceGradleProperties = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/android/gradle.properties')
	var destinationGradleProperties = path.join(context.opts.projectRoot,'platforms/android/gradle.properties')
	fs.createReadStream(sourceGradleProperties).pipe(fs.createWriteStream(destinationGradleProperties))
	
	var sourceBuildGradle = path.join(context.opts.projectRoot,'SCBL_Ionic_RN_Plugin_New/ReactNative/src/android/build.gradle')
	var destinationBuildGradle = path.join(context.opts.projectRoot,'platforms/android/build.gradle')
	fs.createReadStream(sourceBuildGradle).pipe(fs.createWriteStream(destinationBuildGradle))
	
	console.log('Replacing MainActivity.java and build.gradle and copying gradle.properties done.')
}