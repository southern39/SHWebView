# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn java.lang.invoke.StringConcatFactory

## Keep SHWebView class with public methods
#-keepclassmembers class vn.southern.shwebviewlib.SHWebView {
#    public *;
#}
#
#-keep class vn.southern.shwebviewlib.WebViewInitTask
#-keep class vn.southern.shwebviewlib.WebViewCacheHolder
#-keep class vn.southern.shwebviewlib.utils.ContextHolder
#
## Keep WebViewListener interface
#-keep interface vn.southern.shwebviewlib.WebViewListener
-keep class vn.southern.shwebviewlib.** {
    *;
}

-keep class vn.southern.shwebviewlib.SHWebView { public *; }