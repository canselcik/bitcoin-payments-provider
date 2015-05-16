package internal;

import com._37coins.bcJsonRpc.BitcoindClientFactory;
import com._37coins.bcJsonRpc.BitcoindInterface;
import com.google.common.primitives.Longs;
import play.db.DB;

import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BitcoindClusters {
    public static class ClusterInfo {
        public String id, connString;
        public BitcoindClientFactory factory;
        public ClusterInfo(String id, String connString, BitcoindClientFactory factory){
            this.id = id;
            this.connString = connString;
            this.factory = factory;
        }
    }

    private static List<ClusterInfo> clusters = new ArrayList<ClusterInfo>();
    public static int loadBitcoindClusters(){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id, conn_string, rpc_username, rpc_password FROM bitcoind_clusters ORDER BY id ASC");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String id = rs.getString("id");
                String connString = rs.getString("conn_string");
                String rpcUsername = rs.getString("rpc_username");
                String rpcPassword = rs.getString("rpc_password");
                BitcoindClientFactory bcf = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
                clusters.add( new ClusterInfo(id, connString, bcf) );
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters.size();
    }

    private static int assignCluster(String user){
        try {
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(user.getBytes("utf8"));
            Long i = Longs.fromByteArray(cript.digest());
            return (int)(i % clusters.size());
        }
        catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static List<ClusterInfo> getClusters(){
        return clusters;
    }

    public static BitcoindInterface getClusterInterface(Integer clusterId){
        if(clusterId >= clusters.size())
            return null;
        return clusters.get(clusterId).factory.getClient();
    }

    public static BitcoindInterface getInterface(String user){
        // TODO: (1) First check if we already have an assignment for this user
        //          (a) If so, return interface for that
        //          (b) If not, run assignCluster(user) and add an entry for the user to DB.
        return clusters.get(assignCluster(user)).factory.getClient();
    }
}
