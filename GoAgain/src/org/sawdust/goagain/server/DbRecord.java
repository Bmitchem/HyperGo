package org.sawdust.goagain.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jdo.annotations.Persistent;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.sawdust.goagain.shared.GameRecord;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

@Entity
public class DbRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Persistent private Key id;
  @Persistent private String key;
  @Persistent private com.google.appengine.api.datastore.Blob blob = null;
  @Persistent private List<String> members = new ArrayList<String>();

  public void setValue(GameRecord gameRecord) {
    this.blob = new Blob(toBytes(gameRecord));
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public GameRecord getValue() {
    return (GameRecord) fromBytes(blob.getBytes());
  }

  public Key getId() {
    return id;
  }

  public static Serializable fromBytes(final byte[] data)
  {
    if(null == data || 0 == data.length)
    {
      return null;
    }
      Serializable copiedObj = null;
      try
      {
          final ByteArrayInputStream inBuffer = new ByteArrayInputStream(data);
          ZipInputStream z = new ZipInputStream(inBuffer);
          z.getNextEntry();
          final ObjectInputStream in = new ObjectInputStream(z);
          copiedObj = (Serializable) in.readObject();
      }
      catch (final IOException e)
      {
          throw new RuntimeException(e);
      }
      catch (final ClassNotFoundException e)
      {
        throw new RuntimeException(e);
      }
      return copiedObj;
  }


  public static <T extends Serializable> byte[] toBytes(final T obj)
  {
      byte[] data = new byte[0];
      try
      {
          final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
          ZipOutputStream z = new ZipOutputStream(outBuffer);
          z.putNextEntry(new ZipEntry(""));
          final ObjectOutputStream out = new ObjectOutputStream(z);
          try
          {
              out.writeObject(obj);
              out.flush();
              z.closeEntry();
              z.flush();
              data = outBuffer.toByteArray();
          }
          catch (Throwable e)
          {
            throw new RuntimeException(e);
          }
      }
      catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
      return data;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  public List<String> getMembers() {
    return members;
  }
}
