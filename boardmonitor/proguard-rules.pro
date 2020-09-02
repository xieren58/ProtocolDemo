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


#保留位于View类中的get和set方法
-keepclassmembers public class * extends android.view.View{
    void set*(***);
    *** get*();
}
#保留在Activity中以View为参数的方法不变
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
#保留实现了Parcelable的类名不变，
-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}
 #保留R$*类中静态成员的变量名
-keepclassmembers class **.R$*{
    public static <fields>;
}