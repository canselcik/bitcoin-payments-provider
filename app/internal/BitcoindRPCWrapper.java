package internal;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;

import java.net.URL;

public class BitcoindRPCWrapper {
    public static String getInfo(String connString, String rpcUser, String rpcPassword) {
        BitcoindClientFactory clientFactory;
        try { clientFactory = new BitcoindClientFactory(new URL(connString), rpcUser, rpcPassword); }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        BitcoindInterface btcdInterface = clientFactory.getClient();
        if(btcdInterface == null)
            return null;
        return btcdInterface.getinfo().toString();
    }
}
