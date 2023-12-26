@tool
extends EditorPlugin

var googleplaybilling : AndroidExportPlugin

func _enter_tree():
	# Initialization of the plugin goes here.
	googleplaybilling = AndroidExportPlugin.new()
	add_export_plugin(googleplaybilling)


func _exit_tree():
	# Clean-up of the plugin goes here.
	remove_export_plugin(googleplaybilling)
	googleplaybilling = null


class AndroidExportPlugin extends EditorExportPlugin:
	var _plugin_name = "GooglePlayBilling"
	
	# Specifies which platform is supported by the plugin.
	func _supports_platform(platform):
		if platform is EditorExportPlatformAndroid:
			return true
		return false

	# Return the paths of the plugin's AAR binaries relative to the 'addons' directory.
	func _get_android_libraries(platform, debug):
		if debug:
			return PackedStringArray(["GooglePlayBilling/GooglePlayBilling.1.2.debug.aar"])
		else:
			return PackedStringArray(["GooglePlayBilling/GooglePlayBilling.1.2.debug.aar"])

	func _get_android_dependencies(platform, debug):
			return PackedStringArray(["com.android.billingclient:billing:6.1.0"])

	# Return the plugin's name.
	func _get_name():
		return _plugin_name
