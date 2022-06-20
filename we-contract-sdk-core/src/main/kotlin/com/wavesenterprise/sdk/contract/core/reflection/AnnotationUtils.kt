package com.wavesenterprise.sdk.contract.core.reflection

import org.apache.commons.lang3.ClassUtils

object AnnotationUtils {

    fun <A : Annotation> findAnnotation(clazz: Class<*>, annotation: Class<A>): A? =
        mutableListOf(clazz).apply {
            addAll(ClassUtils.getAllSuperclasses(clazz))
            addAll(ClassUtils.getAllInterfaces(clazz))
        }.find {
            it.getAnnotation(annotation) != null
        }?.run { getAnnotation(annotation) }
}
