package internal;

import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.pojo.Info;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;


public class Bitcoind {
    public static String getNewAddress(String user) {
        BitcoindInterface btcdInterface = BitcoindClusters.getInterface(user);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getnewaddress(user);
    }

    public static Info getInfo(Integer clusterId){
        BitcoindInterface btcdInterface = BitcoindClusters.getClusterInterface(clusterId);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getinfo();
    }

    public static List<String> getAddresses(String user){
        BitcoindInterface btcdInterface = BitcoindClusters.getInterface(user);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getaddressesbyaccount(user);
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    public static List<ObjectNode> getTransactions(String user, int page){
        BitcoindInterface btcdInterface = BitcoindClusters.getInterface(user);
        if(btcdInterface == null)
            return null;
        int count = 20;
        int offset = count * page;

        List<Transaction> a = btcdInterface.listtransactions(user, count, offset);
        List<ObjectNode> txs = new ArrayList<>();
        for(Transaction tx : a){
            ObjectNode node = mapper.createObjectNode();
            node.put("txid", tx.getTxid());
            node.put("amount", tx.getAmount());
            node.put("time", tx.getTime());
            txs.add(node);
        }
        return txs;
    }
}
