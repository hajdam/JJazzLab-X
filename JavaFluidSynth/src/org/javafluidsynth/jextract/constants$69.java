// Generated by jextract

package org.javafluidsynth.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$69 {

    static final FunctionDescriptor fluid_version$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle fluid_version$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_version",
        "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$69.fluid_version$FUNC, false
    );
    static final FunctionDescriptor fluid_version_str$FUNC = FunctionDescriptor.of(C_POINTER);
    static final MethodHandle fluid_version_str$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_version_str",
        "()Ljdk/incubator/foreign/MemoryAddress;",
        constants$69.fluid_version_str$FUNC, false
    );
    static final FunctionDescriptor fluid_ladspa_is_active$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_ladspa_is_active$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_ladspa_is_active",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$69.fluid_ladspa_is_active$FUNC, false
    );
    static final FunctionDescriptor fluid_ladspa_activate$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_ladspa_activate$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_ladspa_activate",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$69.fluid_ladspa_activate$FUNC, false
    );
    static final FunctionDescriptor fluid_ladspa_deactivate$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_ladspa_deactivate$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_ladspa_deactivate",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$69.fluid_ladspa_deactivate$FUNC, false
    );
    static final FunctionDescriptor fluid_ladspa_reset$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_ladspa_reset$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_ladspa_reset",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$69.fluid_ladspa_reset$FUNC, false
    );
}


