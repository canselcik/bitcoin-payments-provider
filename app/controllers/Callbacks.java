package controllers;

import com._37coins.bcJsonRpc.BitcoindInterface;
import com._37coins.bcJsonRpc.pojo.Transaction;
import internal.BitcoindClusters;
import play.db.DB;
import play.mvc.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Callbacks extends Controller {
    private enum TxDatabasePresence {
        ERROR(-1), NOT_PRESENT(0), UNCONFIRMED(1), CONFIRMED(2);
        private final int id;
        TxDatabasePresence(int id) { this.id = id; }
        public int getValue() { return id; }
    };
    private static TxDatabasePresence txPresentInDB(String txHash){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT internal_txid, matched_user_id, inbound, tx_hash, confirmed FROM transactions WHERE tx_hash = ?");
            ps.setString(1, txHash);
            ResultSet rs = ps.executeQuery();
            c.close();
            if(rs == null)
                return TxDatabasePresence.ERROR;
            if(!rs.next())
                return TxDatabasePresence.NOT_PRESENT;
            if(!rs.getString("tx_hash").equals(txHash))
                return TxDatabasePresence.NOT_PRESENT;

            if(rs.getBoolean("confirmed"))
                return TxDatabasePresence.CONFIRMED;
            else
                return TxDatabasePresence.UNCONFIRMED;
        }
        catch(Exception e){
            e.printStackTrace();
            return TxDatabasePresence.ERROR;
        }
    }

    private static long getIdFromAccountName(String accountName){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT account_id FROM account_holders WHERE account_name = ?");
            ps.setString(1, accountName);
            ResultSet rs = ps.executeQuery();
            c.close();

            if(rs == null)
                return -1;
            if(!rs.next())
                return -1;
            return rs.getLong("account_id");
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean insertTxIntoDB(String txHash, long userId, boolean inbound, boolean confirmed, long amountSatoshis){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("INSERT INTO transactions(matched_user_id, inbound, tx_hash, confirmed, amount_satoshi) VALUES(?, ?, ?, ?, ?)");
            ps.setLong(1, userId);
            ps.setBoolean(2, inbound);
            ps.setString(3, txHash);
            ps.setBoolean(4, confirmed);
            ps.setLong(5, amountSatoshis);
            int result = ps.executeUpdate();
            c.close();
            boolean ret = (result == 1);
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean updateTxStatus(String txHash, boolean confirmed){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("UPDATE transactions SET confirmed = ? WHERE tx_hash = ?");
            ps.setBoolean(1, confirmed);
            ps.setString(2, txHash);
            int result = ps.executeUpdate();
            c.close();
            boolean ret = (result == 1);
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static long getUserBalance(long userId, boolean confirmed){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT ? FROM account_holders WHERE account_id = ?");

            ps.setLong(2, userId);
            if(confirmed)
                ps.setString(1, "confirmed_satoshi_balance AS balance");
            else
                ps.setString(1, "unconfirmed_satoshi_balance AS balance");

            ResultSet rs = ps.executeQuery();
            c.close();

            if(rs == null)
                return -1;
            if(!rs.next())
                return -1;
            return rs.getLong("balance");
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean updateUserBalance(long userId, boolean confirmed, long updatedValue){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("UPDATE account_holders SET ? = ? WHERE account_id = ?");
            if(confirmed)
                ps.setString(1, "confirmed_satoshi_balance");
            else
                ps.setString(1, "unconfirmed_satoshi_balance");

            ps.setLong(2, updatedValue);
            ps.setLong(3, userId);

            int updatedRows = ps.executeUpdate();
            c.close();

            boolean res = (updatedRows == 1);
            return res;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static Result txNotify(String payload) {
        // TODO: Figure out a way to pick cluster, maybe picking at random might make sense.
        BitcoindInterface bi = BitcoindClusters.getClusterInterface(1);
        Transaction tx = bi.gettransaction(payload);
        long confirmations = tx.getConfirmations();
        String account = tx.getAccount();
        BigDecimal amount = tx.getAmount();

        // TODO: Figure out a way to programmatically find out if tx is inbound or outbound
        boolean txInbound = true;
        if(!txInbound)
            return internalServerError("Outbound tx requires no additional balance bookkeeping on txnotify");

        if(confirmations == 0){
            TxDatabasePresence presence = txPresentInDB(payload);
            if(presence.getValue() != TxDatabasePresence.NOT_PRESENT.getValue())
                return internalServerError("DatabasePresence for the tx is not 'NOT_PRESENT'");

            long relevantUserId = getIdFromAccountName(account);
            if(relevantUserId < 0)
                return internalServerError("Related user account cannot be found");

            long amountInSAT = amount.multiply(BigDecimal.valueOf(10 ^ 8)).longValue();
            boolean txDbPushResult = insertTxIntoDB(payload, relevantUserId, txInbound, false, amountInSAT);
            if(!txDbPushResult)
                return internalServerError("Failed to commit the tx into the DB");

            // Since we are dealing with an unconfirmed tx, we will increment the unconfirmed balance
            long unconfirmedUserBalance = getUserBalance(relevantUserId, false);
            if(unconfirmedUserBalance < 0)
                return internalServerError("Failed to retrieve unconfirmed user balance before updating it");

            boolean updateBalanceResult = updateUserBalance(relevantUserId, false, unconfirmedUserBalance + amountInSAT);
            if(updateBalanceResult)
                return ok("Transaction processed");
            else
                return internalServerError("Failed to update user balance");
        }
        else {
            // TODO: Check if we have the transaction's inputs in our used txo table
            // TODO: If it isn't, mark the transaction confirmed, decrement unconfirmed balance, increment confirmed balance
            // TODO: Add the transaction inputs to the used txos table.
        }

        return Results.TODO;
    }

    public static Result blockNotify(String payload) {
        return Results.TODO;
    }
}
