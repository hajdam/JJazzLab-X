// Generated by jextract

package org.javafluidsynth.jextract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
public interface fluid_sfloader_load_t {

    jdk.incubator.foreign.MemoryAddress apply(jdk.incubator.foreign.MemoryAddress x0, jdk.incubator.foreign.MemoryAddress x1);
    static MemoryAddress allocate(fluid_sfloader_load_t fi) {
        return RuntimeHelper.upcallStub(fluid_sfloader_load_t.class, fi, constants$29.fluid_sfloader_load_t$FUNC, "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)Ljdk/incubator/foreign/MemoryAddress;");
    }
    static MemoryAddress allocate(fluid_sfloader_load_t fi, ResourceScope scope) {
        return RuntimeHelper.upcallStub(fluid_sfloader_load_t.class, fi, constants$29.fluid_sfloader_load_t$FUNC, "(Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)Ljdk/incubator/foreign/MemoryAddress;", scope);
    }
    static fluid_sfloader_load_t ofAddress(MemoryAddress addr) {
        return (jdk.incubator.foreign.MemoryAddress x0, jdk.incubator.foreign.MemoryAddress x1) -> {
            try {
                return (jdk.incubator.foreign.MemoryAddress)constants$29.fluid_sfloader_load_t$MH.invokeExact((Addressable)addr, x0, x1);
            } catch (Throwable ex$) {
                throw new AssertionError("should not reach here", ex$);
            }
        };
    }
}


