package com.example.block_call

import android.app.Activity
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.canCurrentUserBlockNumbers
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity: FlutterActivity() {

    private val CHANNEL = "murtaza.com/block"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SET_DEFAULT_DIALER -> checkSetDefaultDialerResult(resultCode)
        }
    }
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {

        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->

            if (call.method == "REQ") {

                checkDefaultDialer(activity)

            }

                if (call.method == "BLOCK") {
                    val arguments = call.arguments()     as Map<String,String>
                    val number =  arguments["number"]
                if(canCurrentUserBlockNumbers(context)) {
                    val values = ContentValues()
                    values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                    val uri: Uri? = contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
//                requestScreeningRole();

                        Toast.makeText(context, "USER IS BLOCKED", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, "USER CANNOT BE BLOCKED", Toast.LENGTH_LONG).show();

                }
            }
            if(call.method == "UNBLOCK"){
                val arguments = call.arguments()     as Map<String,String>
                val number =  arguments["number"]
                val values = ContentValues()
                values.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                val uri = contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
                contentResolver.delete(uri!!, null, null)
            }

        }
    }

    // *** Making Default Dialer App ***

     val REQUEST_CODE_SET_DEFAULT_DIALER=200

    private fun checkDefaultDialer( context : Activity) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Toast.makeText(this, "INTO ASK DIALOG", Toast.LENGTH_LONG).show()
            val rm = getSystemService(ROLE_SERVICE) as RoleManager
            startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), 120)

        } else {
            val telecomManager = getSystemService(context, TelecomManager::class.java) as TelecomManager
            val isAlreadyDefaultDialer = packageName == telecomManager.defaultDialerPackage
            if (isAlreadyDefaultDialer)
                return
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            val bundle: Bundle? = null

            startActivityForResult(context, intent, REQUEST_CODE_SET_DEFAULT_DIALER, bundle)
        }
    }





    private fun checkSetDefaultDialerResult(resultCode: Int) {
        val message = when (resultCode) {
            RESULT_OK       -> "User accepted request to become default dialer"
            RESULT_CANCELED -> "User declined request to become default dialer"
            else            -> "Unexpected result code $resultCode"
        }

    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}











// CALL RECIEVERS
//@SuppressLint("PrivateApi")
//fun endCall(context: Context): Boolean {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
//
//            telecomManager.endCall()
//            return true
//        }
//        return false
//    }
//    //use unofficial API for older Android versions, as written here: https://stackoverflow.com/a/8380418/878126
//    try {
//        val telephonyClass = Class.forName("com.android.internal.telephony.ITelephony")
//        val telephonyStubClass = telephonyClass.classes[0]
//        val serviceManagerClass = Class.forName("android.os.ServiceManager")
//        val serviceManagerNativeClass = Class.forName("android.os.ServiceManagerNative")
//        val getService = serviceManagerClass.getMethod("getService", String::class.java)
//        val tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface",
//                IBinder::class.java)
//        val tmpBinder = Binder()
//        tmpBinder.attachInterface(null, "fake")
//        val serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder)
//        val retbinder = getService.invoke(serviceManagerObject, "phone") as IBinder
//        val serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder::class.java)
//        val telephonyObject = serviceMethod.invoke(null, retbinder)
//        val telephonyEndCall = telephonyClass.getMethod("endCall")
//        telephonyEndCall.invoke(telephonyObject)
//        return true
//    } catch (e: Exception) {
//        e.printStackTrace()
//        return false
//    }
//}
//
//
//
//
//class IncomingCallReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val telephonyService: ITelephony
//        try {
//            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//            val number = intent.getExtras()?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING, ignoreCase = true)) {
//                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                try {
//                    val m = tm.javaClass.getDeclaredMethod("getITelephony")
//                    m.isAccessible = true
//                    telephonyService = m.invoke(tm) as ITelephony
//                        if (number =="+917879301352") {
//                        telephonyService.endCall()
//                        Toast.makeText(context, "Ending the call from: $number", Toast.LENGTH_SHORT).show()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                Toast.makeText(context, "Ring $number", Toast.LENGTH_SHORT).show()
//            }
//            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK, ignoreCase = true)) {
//                Toast.makeText(context, "Answered $number", Toast.LENGTH_SHORT).show()
//            }
//            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE, ignoreCase = true)) {
//                Toast.makeText(context, "Idle $number", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
//
//interface ITelephony {
//    fun endCall(): Boolean
//    fun answerRingingCall()
//    fun silenceRinger()
//}


//// using third way:
//
//@RequiresApi(api = Build.VERSION_CODES.N)
//class ScreeningService : CallScreeningService() {
//
//    override fun onScreenCall(details: Call.Details) {
//        //code here
//    }
//
//}}
//


//!FOURTH WAY:



//
//fun bloclCall(){
//    val CallBlocker: BroadcastReceiver
//    var telephonyManager: TelephonyManager
//    var telephonyService: ITelephony
//    CallBlocker = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            // TODO Auto-generated method stub
//            telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            //Java Reflections
//            var c: Class<*>? = null
//            try {
//                c = Class.forName(telephonyManager.getName().getClass())
//            } catch (e: ClassNotFoundException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            }
//            var m: Method? = null
//            try {
//                m = c!!.getDeclaredMethod("getITelephony")
//            } catch (e: SecurityException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            } catch (e: NoSuchMethodException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            }
//            m.setAccessible(true)
//            try {
//                telephonyService = m.invoke(telephonyManager) as ITelephony
//            } catch (e: IllegalArgumentException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            } catch (e: IllegalAccessException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            } catch (e: InvocationTargetException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            }
//            telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_CALL_STATE)
//        } //onReceive()
//
//        var callBlockListener: PhoneStateListener = object : PhoneStateListener() {
//            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
//                if (state == TelephonyManager.CALL_STATE_RINGING) {
//                    if (blockAll_cb.isChecked()) {
//                        try {
//                            telephonyService.endCall()
//                        } catch (e: RemoteException) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//        }
//    } //BroadcastReceiver

//    val filter = IntentFilter("android.intent.action.PHONE_STATE")
//    registerReceiver(CallBlocker, filter)
//}

//
//internal interface ITelephony {
//    fun endCall(): Boolean
//    fun answerRingingCall()
//    fun silenceRinger()
//}