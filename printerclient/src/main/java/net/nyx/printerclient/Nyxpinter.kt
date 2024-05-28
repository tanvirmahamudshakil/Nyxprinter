package net.nyx.printerclient

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import java.util.ServiceConfigurationError


class Nyxpinter {
    fun getInstance(): Nyxpinter {
        return Nyxpinter.SingletonContainer.instance
    }

    fun bindService(mContext: Context?, callback: ServiceConnection?): Boolean {
        return if (mContext != null && callback != null) {
            val intent = Intent()
            intent.setPackage("net.nyx.printerservice")
            intent.setAction("net.nyx.printerservice.IPrinterService")
            mContext.applicationContext.bindService(intent, callback, Context.BIND_AUTO_CREATE)
        } else {
            throw ServiceConfigurationError("parameter must be not null!")
        }
    }


    fun unBindService(mContext: Context?, callback: ServiceConnection?) {
        if (mContext != null && callback != null) {
            mContext.applicationContext.unbindService(callback)
        } else {
            throw ServiceConfigurationError("parameter must be not null!")
        }
    }


//
//     fun bindService() {
//        val intent = Intent()
//        intent.setPackage("net.nyx.printerservice")
//        intent.setAction("net.nyx.printerservice.IPrinterService")
//        context.bindService(intent, connService, Context.BIND_AUTO_CREATE)
//    }
//
//    fun unbindService() {
//        context.unbindService(connService)
//    }


    private object SingletonContainer {
        val instance: Nyxpinter =
            Nyxpinter()
    }

}