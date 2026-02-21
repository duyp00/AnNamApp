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

-dontwarn java.sql.JDBCType
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.ElementVisitor
-dontwarn javax.lang.model.element.ExecutableElement
-dontwarn javax.lang.model.element.Name
-dontwarn javax.lang.model.element.PackageElement
-dontwarn javax.lang.model.element.TypeElement
-dontwarn javax.lang.model.element.TypeParameterElement
-dontwarn javax.lang.model.element.VariableElement
-dontwarn javax.lang.model.type.ArrayType
-dontwarn javax.lang.model.type.DeclaredType
-dontwarn javax.lang.model.type.ExecutableType
-dontwarn javax.lang.model.type.TypeKind
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVariable
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.ElementFilter
-dontwarn javax.lang.model.util.SimpleElementVisitor8
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn javax.lang.model.util.Types

# ---- Retrofit ----
# Keep Retrofit service interfaces and their annotations so R8 doesn't remove
# the interface methods that are only called reflectively via Proxy.
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Keep OkHttp ResponseBody so Retrofit can deserialize streaming responses.
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Gson (retrofit2-converter-gson) ----
# Keep fields of classes that are serialized/deserialized by Gson so their
# names are not removed or renamed (Gson maps JSON keys to field names).
-keepclassmembers class com.example.annamapp.networking.ResponseJSON {
    <fields>;
}

# ---- kotlinx.serialization ----
# Keep the serializer companion objects and descriptors that the runtime
# looks up by reflection. Without these the app crashes with a
# "Serializer for class X is not found" exception.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static **$serializer INSTANCE;
    static kotlinx.serialization.KSerializer serializer(...);
    static kotlinx.serialization.descriptors.SerialDescriptor serialDescriptor(...);
    kotlinx.serialization.KSerializer serializer();
}
# Keep all @Serializable sealed interface hierarchy members used for
# type-safe Navigation (Routes) so their qualified names survive shrinking.
-keep @kotlinx.serialization.Serializable class com.example.annamapp.navigation.Routes { *; }
-keep @kotlinx.serialization.Serializable class * implements com.example.annamapp.navigation.Routes { *; }
