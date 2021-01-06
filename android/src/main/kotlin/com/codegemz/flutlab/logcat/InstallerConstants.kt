package com.codegemz.flutlab.logcat

object InstallerConstants {
    const val INCOMING_MESSAGE_LOG_LINE_KEY = 1
    const val INCOMING_MESSAGE_UNHANDLED_EXCEPTION = 2
    const val INCOMING_MESSAGE_AWAIT_PEER_CONNECTION = 3
    const val OUTCOMING_MESSAGE_AWAIT_PEER_CONNECTION_RESULT = 1
    const val LINE_KEY = "line"
    const val PACKAGE_NAME_KEY = "packageName"
    const val EXCEPTION_KEY = "exception"
    const val THREAD_KEY = "thread"
    const val THREAD_NAME_KEY = "name"
    const val AWAIT_PEER_CONNECTION_RESULT_KEY = "result"
    const val AWAIT_PEER_CONNECTION_RESULT_SUCCESS = 1
    const val AWAIT_PEER_CONNECTION_RESULT_FAILURE = 2
    const val ERROR_MESSAGE_KEY = "error_message"

    const val THROWABLE_CLASS_NAME_KEY = "className"
    const val THROWABLE_MESSAGE_KEY = "message"
    const val THROWABLE_CAUSE_KEY = "cause"
    const val THROWABLE_STACK_TRACE_KEY = "stackTrace"
    const val STACK_TRACE_ELEMENT_CLASS_NAME_KEY = "className"
    const val STACK_TRACE_ELEMENT_METHOD_NAME_KEY = "methodName"
    const val STACK_TRACE_ELEMENT_FILE_NAME_KEY = "fileName"
    const val STACK_TRACE_ELEMENT_LINE_NUMBER_KEY = "lineNumber"
    const val STACK_TRACE_ELEMENT_IS_NATIVE_METHOD_KEY = "isNativeMethod"
}