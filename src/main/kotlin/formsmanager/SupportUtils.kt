package formsmanager

import org.slf4j.Logger

fun Logger.ifDebugEnabled(msg: () -> String){
    if (this.isDebugEnabled){
        this.debug(msg.invoke())
    }
}