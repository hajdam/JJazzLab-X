// Generated by jextract

package org.javafluidsynth.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$3 {

    static final FunctionDescriptor fluid_settings_foreach_option_t$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_settings_foreach_option_t$MH = RuntimeHelper.downcallHandle(
        "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$3.fluid_settings_foreach_option_t$FUNC, false
    );
    static final FunctionDescriptor fluid_settings_foreach_option$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_settings_foreach_option$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_settings_foreach_option",
        "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$3.fluid_settings_foreach_option$FUNC, false
    );
    static final FunctionDescriptor fluid_settings_option_count$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_settings_option_count$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_settings_option_count",
        "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$3.fluid_settings_option_count$FUNC, false
    );
    static final FunctionDescriptor fluid_settings_option_concat$FUNC = FunctionDescriptor.of(C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_settings_option_concat$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_settings_option_concat",
        "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)Ljdk/incubator/foreign/MemoryAddress;",
        constants$3.fluid_settings_option_concat$FUNC, false
    );
    static final FunctionDescriptor fluid_settings_foreach_t$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER,
        C_POINTER,
        C_INT
    );
}


