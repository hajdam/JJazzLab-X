// Generated by jextract

package org.jjazz.fluidsynthjava.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$49 {

    static final FunctionDescriptor handle_midi_tick_func_t$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT
    );
    static final MethodHandle handle_midi_tick_func_t$MH = RuntimeHelper.downcallHandle(
        "(Ljdk/incubator/foreign/MemoryAddress;I)I",
        constants$49.handle_midi_tick_func_t$FUNC, false
    );
    static final FunctionDescriptor new_fluid_midi_event$FUNC = FunctionDescriptor.of(C_POINTER);
    static final MethodHandle new_fluid_midi_event$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "new_fluid_midi_event",
        "()Ljdk/incubator/foreign/MemoryAddress;",
        constants$49.new_fluid_midi_event$FUNC, false
    );
    static final FunctionDescriptor delete_fluid_midi_event$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER
    );
    static final MethodHandle delete_fluid_midi_event$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "delete_fluid_midi_event",
        "(Ljdk/incubator/foreign/MemoryAddress;)V",
        constants$49.delete_fluid_midi_event$FUNC, false
    );
    static final FunctionDescriptor fluid_midi_event_set_type$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT
    );
    static final MethodHandle fluid_midi_event_set_type$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_midi_event_set_type",
        "(Ljdk/incubator/foreign/MemoryAddress;I)I",
        constants$49.fluid_midi_event_set_type$FUNC, false
    );
    static final FunctionDescriptor fluid_midi_event_get_type$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER
    );
    static final MethodHandle fluid_midi_event_get_type$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_midi_event_get_type",
        "(Ljdk/incubator/foreign/MemoryAddress;)I",
        constants$49.fluid_midi_event_get_type$FUNC, false
    );
    static final FunctionDescriptor fluid_midi_event_set_channel$FUNC = FunctionDescriptor.of(C_INT,
        C_POINTER,
        C_INT
    );
    static final MethodHandle fluid_midi_event_set_channel$MH = RuntimeHelper.downcallHandle(
        fluidsynth_h.LIBRARIES, "fluid_midi_event_set_channel",
        "(Ljdk/incubator/foreign/MemoryAddress;I)I",
        constants$49.fluid_midi_event_set_channel$FUNC, false
    );
}

