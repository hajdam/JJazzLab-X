// Generated by jextract

package org.jjazz.fluidsynthjava.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$23 {

    static final FunctionDescriptor fluid_synth_process$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT,
        C_POINTER,
        C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_synth_process$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_process",
        "(Ljdk/incubator/foreign/MemoryAddress;IILjdk/incubator/foreign/MemoryAddress;ILjdk/incubator/foreign/MemoryAddress;)I",
        constants$23.fluid_synth_process$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_custom_filter$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT
    );
    static final MethodHandle fluid_synth_set_custom_filter$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_custom_filter",
        "(Ljdk/incubator/foreign/MemoryAddress;II)I",
        constants$23.fluid_synth_set_custom_filter$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_channel_type$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT
    );
    static final MethodHandle fluid_synth_set_channel_type$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_channel_type",
        "(Ljdk/incubator/foreign/MemoryAddress;II)I",
        constants$23.fluid_synth_set_channel_type$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_reset_basic_channel$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT
    );
    static final MethodHandle fluid_synth_reset_basic_channel$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_reset_basic_channel",
        "(Ljdk/incubator/foreign/MemoryAddress;I)I",
        constants$23.fluid_synth_reset_basic_channel$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_get_basic_channel$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_synth_get_basic_channel$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_get_basic_channel",
        "(Ljdk/incubator/foreign/MemoryAddress;ILjdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$23.fluid_synth_get_basic_channel$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_basic_channel$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT,
        C_INT
    );
    static final MethodHandle fluid_synth_set_basic_channel$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_basic_channel",
        "(Ljdk/incubator/foreign/MemoryAddress;III)I",
        constants$23.fluid_synth_set_basic_channel$FUNC, false
    );
}

