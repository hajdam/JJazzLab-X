// Generated by jextract

package org.javafluidsynth.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$17 {

    static final FunctionDescriptor fluid_synth_set_chorus_group_nr$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT
    );
    static final MethodHandle fluid_synth_set_chorus_group_nr$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_chorus_group_nr",
        "(Ljdk/incubator/foreign/MemoryAddress;II)I",
        constants$17.fluid_synth_set_chorus_group_nr$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_chorus_group_level$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_DOUBLE
    );
    static final MethodHandle fluid_synth_set_chorus_group_level$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_chorus_group_level",
        "(Ljdk/incubator/foreign/MemoryAddress;ID)I",
        constants$17.fluid_synth_set_chorus_group_level$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_chorus_group_speed$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_DOUBLE
    );
    static final MethodHandle fluid_synth_set_chorus_group_speed$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_chorus_group_speed",
        "(Ljdk/incubator/foreign/MemoryAddress;ID)I",
        constants$17.fluid_synth_set_chorus_group_speed$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_chorus_group_depth$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_DOUBLE
    );
    static final MethodHandle fluid_synth_set_chorus_group_depth$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_chorus_group_depth",
        "(Ljdk/incubator/foreign/MemoryAddress;ID)I",
        constants$17.fluid_synth_set_chorus_group_depth$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_set_chorus_group_type$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_INT
    );
    static final MethodHandle fluid_synth_set_chorus_group_type$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_set_chorus_group_type",
        "(Ljdk/incubator/foreign/MemoryAddress;II)I",
        constants$17.fluid_synth_set_chorus_group_type$FUNC, false
    );
    static final FunctionDescriptor fluid_synth_get_chorus_group_nr$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_synth_get_chorus_group_nr$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_synth_get_chorus_group_nr",
        "(Ljdk/incubator/foreign/MemoryAddress;ILjdk/incubator/foreign/MemoryAddress;)I",
        constants$17.fluid_synth_get_chorus_group_nr$FUNC, false
    );
}


