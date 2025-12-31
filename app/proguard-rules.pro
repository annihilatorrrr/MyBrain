# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    *;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.mhss.app.widget.** { *; }

-keep class * extends ai.koog.agents.core.tools.reflect.ToolSet { *; }

-keep enum * { *; }


-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,MethodParameters

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn brave.Span
-dontwarn brave.Tracer
-dontwarn brave.Tracing
-dontwarn brave.propagation.TraceContext
-dontwarn com.aayushatharva.brotli4j.encoder.Encoder$Mode
-dontwarn com.google.auto.value.AutoValue
-dontwarn io.micrometer.common.docs.KeyName
-dontwarn io.micrometer.context.ContextAccessor
-dontwarn io.micrometer.context.ContextSnapshot
-dontwarn io.micrometer.context.ContextSnapshotFactory$Builder
-dontwarn io.micrometer.context.ContextSnapshotFactory
-dontwarn io.micrometer.observation.docs.ObservationDocumentation
-dontwarn io.netty.channel.epoll.EpollDatagramChannel
-dontwarn io.netty.channel.epoll.EpollDomainSocketChannel
-dontwarn io.netty.channel.epoll.EpollEventLoopGroup
-dontwarn io.netty.channel.epoll.EpollSocketChannel
-dontwarn io.netty.channel.kqueue.KQueue
-dontwarn io.netty.channel.kqueue.KQueueDatagramChannel
-dontwarn io.netty.channel.kqueue.KQueueDomainSocketChannel
-dontwarn io.netty.channel.kqueue.KQueueEventLoopGroup
-dontwarn io.netty.channel.kqueue.KQueueSocketChannel
-dontwarn io.netty.incubator.channel.uring.IOUringDatagramChannel
-dontwarn io.netty.incubator.channel.uring.IOUringEventLoopGroup
-dontwarn io.netty.incubator.channel.uring.IOUringSocketChannel
-dontwarn io.netty.internal.tcnative.Buffer
-dontwarn io.netty.internal.tcnative.CertificateCallback
-dontwarn io.netty.internal.tcnative.Library
-dontwarn io.netty.internal.tcnative.SSL
-dontwarn io.netty.internal.tcnative.SSLContext
-dontwarn io.reactivex.BackpressureStrategy
-dontwarn io.reactivex.Completable
-dontwarn io.reactivex.Flowable
-dontwarn io.reactivex.Maybe
-dontwarn io.reactivex.Observable
-dontwarn io.reactivex.Single
-dontwarn io.reactivex.rxjava3.core.BackpressureStrategy
-dontwarn io.reactivex.rxjava3.core.Completable
-dontwarn io.reactivex.rxjava3.core.Flowable
-dontwarn io.reactivex.rxjava3.core.Maybe
-dontwarn io.reactivex.rxjava3.core.Observable
-dontwarn io.reactivex.rxjava3.core.Single
-dontwarn javax.enterprise.inject.spi.Extension
-dontwarn org.LatencyUtils.PauseDetector
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.Level
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.logging.log4j.message.MessageFactory
-dontwarn org.apache.logging.log4j.spi.ExtendedLogger
-dontwarn org.apache.logging.log4j.spi.ExtendedLoggerWrapper
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
-dontwarn rx.Completable
-dontwarn rx.Observable
-dontwarn rx.RxReactiveStreams
-dontwarn rx.Single
-dontwarn rx.internal.reactivestreams.PublisherAdapter