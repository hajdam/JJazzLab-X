// Generated by jextract

package org.javafluidsynth.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
public interface fluid_sfloader_callback_tell_t {

    long apply(jdk.incubator.foreign.MemoryAddress x0);
    static MemoryAddress allocate(fluid_sfloader_callback_tell_t fi) {
        return RuntimeHelper.upcallStub(fluid_sfloader_callback_tell_t.class, fi, constants$31.fluid_sfloader_callback_tell_t$FUNC, "(Ljdk/incubator/foreign/MemoryAddress;)J");
    }
    static MemoryAddress allocate(fluid_sfloader_callback_tell_t fi, ResourceScope scope) {
        return RuntimeHelper.upcallStub(fluid_sfloader_callback_tell_t.class, fi, constants$31.fluid_sfloader_callback_tell_t$FUNC, "(Ljdk/incubator/foreign/MemoryAddress;)J", scope);
    }
    static fluid_sfloader_callback_tell_t ofAddress(MemoryAddress addr) {
        return (jdk.incubator.foreign.MemoryAddress x0) -> {
            try {
                return (long)constants$31.fluid_sfloader_callback_tell_t$MH.invokeExact((Addressable)addr, x0);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}


