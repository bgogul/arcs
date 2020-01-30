package(default_visibility = ["//visibility:public"])

load(
    "//third_party/java/arcs/build_defs:build_defs.bzl",
    "kt_jvm_and_js_library",
)
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library", "kt_jvm_binary")
load("@rules_java//java:defs.bzl", "java_plugin", "java_library")

licenses(["notice"])

java_plugin(
    name = "particle_annotation_plugin",
    generates_api = 0,
    processor_class = "arcs.core.annotations.ParticleProcessor",
    deps = [":particle_annotation"],
)

kt_jvm_library(
    name = "particle_annotation",
    srcs = ["Annotations.kt",
            "ParticleProcessor.kt",
            "ManifestBuilder.kt",
            ],
    plugins = ["//third_party/java/auto:autoservice_plugin" ],
    deps = ["//third_party/java/auto:auto_service"],
)

kt_jvm_library(
    name = "annotator_test",
    srcs = ["TestAnnotation.kt"],
    # main_class = "arcs.core.annotations.TestAnnotationKt",
    plugins = [":particle_annotation_plugin"],
    deps = [":particle_annotation"]
)