package org.sawdust.goagain.server;

import java.util.List;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.sawdust.goagain.shared.GameData;
import org.sawdust.goagain.shared.GameId;
import org.sawdust.goagain.shared.GameRecord;
import org.sawdust.goagain.shared.GoAI;
import org.sawdust.goagain.shared.GoGame;
import org.sawdust.goagain.shared.GameService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GameServiceImpl extends RemoteServiceServlet implements GameService {

  static PersistenceManagerFactory pmfInstance;

  protected static PersistenceManagerFactory getPmfInstance() {
    if (null == pmfInstance) {
      try {
        try {
          pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");
        } catch (javax.jdo.JDOException e) {
          Properties testProperties = new Properties();
          testProperties.put("javax.jdo.PersistenceManagerFactoryClass",
                    "org.datanucleus.store.appengine.jdo.DatastoreJDOPersistenceManagerFactory");
          testProperties.put("javax.jdo.option.ConnectionURL", "appengine");
          testProperties.put("javax.jdo.option.NontransactionalRead", "true");
          testProperties.put("javax.jdo.option.NontransactionalWrite", "true");
          testProperties.put("javax.jdo.option.RetainValues", "true");
          testProperties.put("datanucleus.appengine.autoCreateDatastoreTxns", "true");
          Properties initTest = testProperties;
          pmfInstance = JDOHelper.getPersistenceManagerFactory(initTest);
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    return pmfInstance;
  }

  public GameRecord getGame(final GameId key) {
    final String name = key.key;
    return run(new Op<GameRecord>(){
      public GameRecord exe(PersistenceManager em) {
        GameRecord record = getGame(em, name).getValue();
        if (null == record) throw new RuntimeException("Game not found: " + name);
        return record;
      }
    });
  }

  public GoGame move(GoGame game, GoAI goAI) {
    goAI.move(game);
    return game;
  };

  public GameId newGame(final GameData data) {
    return run(new Op<GameId>(){
      public GameId exe(PersistenceManager em) {
        final GameId newId = new GameId(Long.toHexString(System.currentTimeMillis()));
        DbRecord r = new DbRecord();
        r.setValue(new GameRecord(newId, data));
        r.setKey(newId.key);
        em.makePersistent(r);
        return newId;
      }});
  }

  protected <T> T run(Op<T> op) {
    final PersistenceManager em = getPmfInstance().getPersistenceManager();
    try {
      return op.exe(em);
    } finally {
      Transaction currentTransaction = em.currentTransaction();
      if (currentTransaction.isActive())
      {
          currentTransaction.commit();
      }
      em.close();
    }
  }

  public static interface Op<T>
  {
    T exe(final PersistenceManager em);
  }

  public GameId saveGame(final GameId key, final GameData data) {
    final String name = key.key;
    return run(new Op<GameId>(){
      public GameId exe(PersistenceManager em) {
        DbRecord dbRecord = getGame(em, name);
        GameRecord record = dbRecord.getValue();
        if (!record.activeId.equals(key)) 
        {
          throw new RuntimeException("Bad version: " + record.activeId.version + " (expected " + key.version + ")");
        }
        record.activeId = new GameId(key.key, key.version + 1);
        record.data = data;
        dbRecord.setValue(record);
        return record.activeId;
      }
    });
  }
  
  protected DbRecord getGame(PersistenceManager em, final String name) {
    Query newQuery = em.newQuery(DbRecord.class);
    newQuery.setFilter("key == param1");
    newQuery.declareParameters("String param1");
    @SuppressWarnings("unchecked") List<DbRecord> list = (List<DbRecord>) newQuery.execute(name);
    if (0 == list.size()) throw new RuntimeException("No Game Found: " + name);
    if (1 < list.size()) throw new RuntimeException("Multiple Games Found: " + name);
    return list.get(0);
  }

}
