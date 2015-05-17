package internal;

import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.pojo.Info;
import com._37coins.bcJsonRpc.pojo.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Bitcoind {
    private static final BigDecimal TX_FEE = BigDecimal.valueOf(0.0001);
    public static class Pair<T, U> {
        public final T t;
        public final U u;

        public Pair(T t, U u) {
            this.t= t;
            this.u= u;
        }
    }

    public static String getNewAddress(String user) {
        BitcoindInterface btcdInterface = BitcoindClusters.getInterface(user);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getnewaddress(user);
    }

    public static Pair<String, Long> sweepFunds(Integer id, String target) {
        BitcoindInterface btcdInterface = BitcoindClusters.getClusterInterface(id);
        if(btcdInterface == null)
            return new Pair<String, Long>("Cannot find cluster", -1l);
        BigDecimal clusterBalance = btcdInterface.getbalance();
        if(clusterBalance == null)
            return new Pair<String, Long>("Cannot fetch cluster balance", -1l);

        String txHash = btcdInterface.sendtoaddress(target, clusterBalance.subtract(TX_FEE));
        return new Pair<String, Long>(txHash, clusterBalance.multiply(BigDecimal.valueOf(10 ^ 8)).longValueExact());
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
