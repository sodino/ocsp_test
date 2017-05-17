package dns.ocsp.com;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

/**
 * Created by sodino on 2017/5/5.
 */

public class HttpDNS implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        Log.d("Test", "lookup hostname=" + hostname);
        if ("api.meipai.com".equals(hostname)) {
            String ip = "27.148.145.195";
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            Log.d("Test", "inetAddresses:" + inetAddresses);
            return inetAddresses;
        } else {
            return SYSTEM.lookup(hostname);
        }
    }
}
