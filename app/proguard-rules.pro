# 基础优化配置
-optimizationpasses 3                                  # 降低代码压缩级别以减少问题
-dontusemixedcaseclassnames                            # 不使用大小写混合的类名
-dontskipnonpubliclibraryclasses                       # 不跳过非公共库类
-dontskipnonpubliclibraryclassmembers                  # 不跳过非公共库类成员
-dontpreverify                                         # 不预校验
-verbose                                               # 生成详细日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable # 优化选项

# 保留更多调试信息
-keepattributes *Annotation*                           # 保留注解
-keepattributes Signature                              # 保留泛型信息
-keepattributes SourceFile,LineNumberTable            # 保留源文件和行号信息
-keepattributes Exceptions,InnerClasses                # 保留异常和内部类

# 减少混淆强度
-dontobfuscate                                         # 调试阶段可以考虑暂时关闭混淆
# 或使用更温和的混淆设置
# -useuniqueclassmembernames                           # 使用唯一类成员名称

# 不混淆序列化和反序列化相关逻辑
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 不混淆 Android 基本组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends androidx.fragment.app.Fragment

# 保留 R 资源文件
-keep class **.R$* {*;}

# 保留原生方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留View的set和get方法
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# 保留枚举类 (完整保留)
-keep enum * {*;}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 JSON 相关
-keep class org.json.** { *; }

# 保留项目主要类和包
-keep class top.srintelligence.wallpaper_generator.** { *; }
-keep class top.fireworkrocket.lookup_kernel.** { *; }

# 保留数据模型类
-keep class **.model.** { *; }
-keep class **.bean.** { *; }
-keep class **.entity.** { *; }
-keep class **.*Bean { *; }
-keep class **.*Entity { *; }
-keep class **.*Model { *; }

# SQLite
-keep class org.sqlite.** { *; }
-dontwarn org.sqlite.**

# 适当保留可能的序列化/反序列化用到的类
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}