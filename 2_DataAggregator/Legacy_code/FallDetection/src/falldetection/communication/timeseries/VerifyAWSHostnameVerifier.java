package falldetection.communication.timeseries;
//This is a custom Hostname verifier.
//android requires the host to be verified, regardless if the certificate is in keystore, since the IP address isn't "trusted"
//This class checks to make sure the host IP address is the same as the one specified. This allows the app to continue and actually check the server certificate
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import android.util.Log;


public class VerifyAWSHostnameVerifier implements HostnameVerifier {

    public boolean verify(String string, SSLSession sslSession) {
    	Log.i("inside",""+string);
    	
    	if (string.equals( "107.20.150.132"))
        return true;
    	
    	else{
    		Log.i("inside","false "+string);
    		return false;
    	}
    }
}