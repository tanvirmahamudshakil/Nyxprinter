package net.nyx.printerclient

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.TextView
import net.nyx.printerclient.Result.msg
import net.nyx.printerservice.print.IPrinterService
import net.nyx.printerservice.print.PrintTextFormat
import timber.log.Timber
import java.util.concurrent.Executors


class Nyxpinter(var context : Context) {
    private val TAG = "MainActivity"
    protected var btnVer: Button? = null
    protected var btnPaper: Button? = null
    protected var btn1: Button? = null
    protected var btn2: Button? = null
    protected var btn3: Button? = null
    protected var btnScan: Button? = null
    protected var tvLog: TextView? = null

    private val RC_SCAN = 0x99
    var PRN_TEXT: String? = null
    protected var btn4: Button? = null
    protected var btnLbl: Button? = null
    protected var btnLblLearning: Button? = null

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val handler: Handler = Handler()
    var version = arrayOfNulls<String>(1)

    init {
        bindService()
    }



    private var printerService: IPrinterService? = null
    private val connService: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.e( "Connection","printer service disconnected, try reconnect")
            printerService = null
            // 尝试重新bind
            handler.postDelayed({ bindService() }, 5000)
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected: %s", name)
            printerService = IPrinterService.Stub.asInterface(service)
            getVersion()
        }
    }


    protected fun bindService() {
        val intent = Intent()
        intent.setPackage("net.nyx.printerservice")
        intent.setAction("net.nyx.printerservice.IPrinterService")
        context.bindService(intent, connService, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        context.unbindService(connService)
    }



    private val qscReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if ("com.android.NYX_QSC_DATA" == intent.action) {
                val qsc = intent.getStringExtra("qsc")

                printText("qsc-quick-scan-code\n$qsc")
            }
        }
    }

    fun getVersion() : Int {
        var v : Int = -2; // default my set
        singleThreadExecutor.submit {
            try {
                val ret: Int = printerService?.getPrinterVersion(version) ?: -1

                Log.e( "Version","Version: " + msg(ret) + "  " + version[0])
                v = ret
            } catch (e: RemoteException) {
                e.printStackTrace()
                v = -1
            }
        }
        return v;
    }

    private fun paperOut() {
        singleThreadExecutor.submit {
            try {
                printerService?.paperOut(80)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }



     fun printText(text: String?) : Int {
         var v : Int = -2; // default my set
        singleThreadExecutor.submit {
            try {
                val textFormat = PrintTextFormat()
                // textFormat.setTextSize(32);
                // textFormat.setUnderline(true);
                val ret: Int = printerService?.printText(text, textFormat) ?: -1

                Log.e( "Print text","Print text: " + msg(ret))
                if (ret == 0) {
                    paperOut()
                }
                v = ret;
            } catch (e: RemoteException) {
                e.printStackTrace()
                v = -1;
            }
        }
         return  v;
    }

     fun printBarcode() {
        singleThreadExecutor.submit {
            try {
                val ret: Int = printerService?.printBarcode("123456789", 300, 160, 1, 1) ?: -1
                Log.e( "Print text","Print text: " + msg(ret))

                if (ret == 0) {
                    paperOut()
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

     fun printQrCode() {
        singleThreadExecutor.submit {
            try {
                val ret: Int = printerService?.printQrCode("123456789", 300, 300, 1) ?: -1

                Log.e( "Print barcode","Print barcode: " + msg(ret))
                if (ret == 0) {
                    paperOut()
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

     fun printBitmap(bitmap: Bitmap) {
        singleThreadExecutor.submit {
            try {
                val ret: Int = printerService?.printBitmap(
                    bitmap,
                    0,
                    1
                ) ?: -1

                Log.e( "Print bitmap","Print bitmap: " + msg(ret) )
                if (ret == 0) {
                    paperOut()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

     fun printLabel() {
        singleThreadExecutor.submit {
            try {
                var ret: Int = printerService?.labelLocate(240, 16) ?: -1
                if (ret == 0) {
                    val format = PrintTextFormat()
                    printerService?.printText("\nModel:\t\tNB55", format)
                    printerService?.printBarcode("1234567890987654321", 320, 90, 2, 0)

                    printerService?.printText("Time:\t", format)
                    ret = printerService?.labelPrintEnd() ?: -1
                }
                Log.e( "print label","Print label: " + msg(ret))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }






    fun existApp(pkg: String?): Boolean {
        try {
            return pkg?.let { context.getPackageManager().getPackageInfo(it, 0) } != null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    protected fun onDestroy() {

        unbindService()

    }


    fun clearLog() {
        tvLog!!.text = ""
    }
}