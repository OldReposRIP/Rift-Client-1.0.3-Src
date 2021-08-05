package org.spongepowered.asm.mixin.extensibility;

import org.apache.logging.log4j.Level;

public interface IMixinErrorHandler {

    IMixinErrorHandler.ErrorAction onPrepareError(IMixinConfig imixinconfig, Throwable throwable, IMixinInfo imixininfo, IMixinErrorHandler.ErrorAction imixinerrorhandler_erroraction);

    IMixinErrorHandler.ErrorAction onApplyError(String s, Throwable throwable, IMixinInfo imixininfo, IMixinErrorHandler.ErrorAction imixinerrorhandler_erroraction);

    public static enum ErrorAction {

        NONE(Level.INFO), WARN(Level.WARN), ERROR(Level.FATAL);

        public final Level logLevel;

        private ErrorAction(Level logLevel) {
            this.logLevel = logLevel;
        }
    }
}
