package com;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class AirPlay
{
  public static final String DNSSD_TYPE = "_airplay._tcp.local.";
  public static final String NONE = "None";
  public static final String SLIDE_LEFT = "SlideLeft";
  public static final String SLIDE_RIGHT = "SlideRight";
  public static final String DISSOLVE = "Dissolve";
  public static final String USERNAME = "Airplay";
  public static final int PORT = 7000;
  protected String hostname;
  protected String name;
  protected int port;
  protected PhotoThread photothread;
  protected String password;
  protected String authorization;
  protected Auth auth;

  public AirPlay(Service paramService)
  {
    this(hostname, port, name);
  }

  public AirPlay(String paramString)
  {
    this(paramString, 7000);
  }

  public AirPlay(String paramString, int paramInt)
  {
    this(paramString, paramInt, paramString);
  }

  public AirPlay(String paramString1, int paramInt, String paramString2)
  {
    hostname = paramString1;
    port = paramInt;
    name = paramString2;
  }

  public void setPassword(String paramString)
  {
    password = paramString;
  }

  public void setAuth(Auth paramAuth)
  {
    auth = paramAuth;
  }

  protected String md5Digest(String paramString)
  {
    byte[] arrayOfByte1;
    try
    {
      arrayOfByte1 = paramString.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      arrayOfByte1 = paramString.getBytes();
    }
    String str = null;
    char[] arrayOfChar1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    try
    {
      MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
      localMessageDigest.update(arrayOfByte1);
      byte[] arrayOfByte2 = localMessageDigest.digest();
      char[] arrayOfChar2 = new char[32];
      int i = 0;
      for (int j = 0; j < 16; j++)
      {
        int k = arrayOfByte2[j];
        arrayOfChar2[(i++)] = arrayOfChar1[(k >>> 4 & 0xF)];
        arrayOfChar2[(i++)] = arrayOfChar1[(k & 0xF)];
      }
      str = new String(arrayOfChar2);
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return str;
  }

  protected String makeAuthorization(Map paramMap, String paramString1, String paramString2, String paramString3)
  {
    String str1 = (String)paramMap.get("realm");
    String str2 = (String)paramMap.get("nonce");
    String str3 = md5Digest("Airplay:" + str1 + ":" + paramString1);
    String str4 = md5Digest(paramString2 + ":" + paramString3);
    String str5 = md5Digest(str3 + ":" + str2 + ":" + str4);
    authorization = ("Digest username=\"Airplay\", realm=\"" + str1 + "\", " + "nonce=\"" + str2 + "\", " + "uri=\"" + paramString3 + "\", " + "response=\"" + str5 + "\"");
    return authorization;
  }

  protected Map getAuthParams(String paramString)
  {
    HashMap localHashMap = new HashMap();
    int i = paramString.indexOf(' ');
    String str1 = paramString.substring(0, i);
    String str2 = paramString.substring(i + 1).replaceAll("\r\n", " ");
    String[] arrayOfString = str2.split("\", ");
    for (int j = 0; j < arrayOfString.length; j++)
    {
      int k = arrayOfString[j].indexOf("=\"");
      String str3 = arrayOfString[j].substring(0, k);
      String str4 = arrayOfString[j].substring(k + 2);
      if (str4.charAt(str4.length() - 1) == '"') {
        str4 = str4.substring(0, str4.length() - 1);
      }
      localHashMap.put(str3, str4);
    }
    return localHashMap;
  }

  protected String getResponse(HttpURLConnection paramHttpURLConnection, String paramString1, String paramString2)
    throws IOException
  {
    String str1 = (String)((List)paramHttpURLConnection.getHeaderFields().get("WWW-Authenticate")).get(0);
    Map localMap = getAuthParams(str1);
    if (password != null) {
      return makeAuthorization(localMap, password, paramString1, paramString2);
    }
    if (auth != null)
    {
      String str2 = auth.getPassword(hostname, name);
      if (str2 != null) {
        return makeAuthorization(localMap, str2, paramString1, paramString2);
      }
      return null;
    }
    throw new IOException("Authorisation requied");
  }

  protected String doHTTP(String paramString1, String paramString2)
    throws IOException
  {
    return doHTTP(paramString1, paramString2, null);
  }

  protected String doHTTP(String paramString1, String paramString2, ByteArrayOutputStream paramByteArrayOutputStream)
    throws IOException
  {
    return doHTTP(paramString1, paramString2, paramByteArrayOutputStream, null);
  }

  protected String doHTTP(String paramString1, String paramString2, ByteArrayOutputStream paramByteArrayOutputStream, Map paramMap)
    throws IOException
  {
    return doHTTP(paramString1, paramString2, paramByteArrayOutputStream, new HashMap(), true);
  }

  protected String doHTTP(String paramString1, String paramString2, ByteArrayOutputStream paramByteArrayOutputStream, Map paramMap, boolean paramBoolean)
    throws IOException
  {
    URL localURL = null;
    try
    {
      localURL = new URL("http://" + hostname + ":" + port + paramString2);
    }
    catch (MalformedURLException localMalformedURLException) {}
    HttpURLConnection localHttpURLConnection = (HttpURLConnection)localURL.openConnection();
    localHttpURLConnection.setUseCaches(false);
    localHttpURLConnection.setDoOutput(true);
    localHttpURLConnection.setRequestMethod(paramString1);
    if (authorization != null) {
      paramMap.put("Authorization", authorization);
    }
    if (paramMap.size() > 0)
    {
      localHttpURLConnection.setRequestProperty("User-Agent", "MediaControl/1.0");
      localObject = paramMap.keySet().toArray();
      for (int i = 0; i < localObject.length; i++) {
        localHttpURLConnection.setRequestProperty((String)localObject[i], (String)paramMap.get(localObject[i]));
      }
    }
    if (paramByteArrayOutputStream != null)
    {
      localObject = paramByteArrayOutputStream.toByteArray();
      localHttpURLConnection.setRequestProperty("Content-Length", "" + localObject.length);
    }
    localHttpURLConnection.connect();
    if (paramByteArrayOutputStream != null)
    {
      paramByteArrayOutputStream.writeTo(localHttpURLConnection.getOutputStream());
      paramByteArrayOutputStream.flush();
      paramByteArrayOutputStream.close();
    }
    if (localHttpURLConnection.getResponseCode() == 401)
    {
      if (paramBoolean)
      {
        localObject = getResponse(localHttpURLConnection, paramString1, paramString2);
        if (localObject != null) {
          return doHTTP(paramString1, paramString2, paramByteArrayOutputStream, paramMap, false);
        }
        return null;
      }
      throw new IOException("Incorrect password");
    }
    Object localObject = localHttpURLConnection.getInputStream();
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader((InputStream)localObject));
    StringBuffer localStringBuffer = new StringBuffer();
    String str;
    while ((str = localBufferedReader.readLine()) != null)
    {
      localStringBuffer.append(str);
      localStringBuffer.append("\r\n");
    }
    localBufferedReader.close();
    return localStringBuffer.toString();
  }

  public void stop()
  {
    try
    {
      stopPhotoThread();
      doHTTP("POST", "/stop");
    }
    catch (Exception localException) {}
  }

  public void photo(String paramString)
    throws IOException
  {
    photo(paramString, "None");
  }

  public void photo(String paramString1, String paramString2)
    throws IOException
  {
    photo(new File(paramString1), paramString2);
  }

  public void photo(File paramFile)
    throws IOException
  {
    photo(paramFile, "None");
  }

  public void photo(File paramFile, String paramString)
    throws IOException
  {
    BufferedImage localBufferedImage = ImageIO.read(paramFile);
    photo(localBufferedImage, paramString);
  }

  public void photo(BufferedImage paramBufferedImage)
    throws IOException
  {
    photo(paramBufferedImage, "None");
  }

  public void photo(BufferedImage paramBufferedImage, String paramString)
    throws IOException
  {
    stopPhotoThread();
    photoRaw(paramBufferedImage, paramString);
    photothread = new PhotoThread(this, paramBufferedImage, 5000);
    photothread.start();
  }

  public void photoRaw(BufferedImage paramBufferedImage, String paramString)
    throws IOException
  {
    HashMap localHashMap = new HashMap();
    localHashMap.put("X-Apple-Transition", paramString);
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    boolean bool = ImageIO.write(paramBufferedImage, "PNG", localByteArrayOutputStream);
    doHTTP("PUT", "/photo", localByteArrayOutputStream, localHashMap);
  }

  public static BufferedImage captureScreen()
    throws AWTException
  {
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    Dimension localDimension = localToolkit.getScreenSize();
    Rectangle localRectangle = new Rectangle(localDimension);
    Robot localRobot = new Robot();
    BufferedImage localBufferedImage = localRobot.createScreenCapture(localRectangle);
    return localBufferedImage;
  }

  public void stopPhotoThread()
  {
    if (photothread != null)
    {
      photothread.interrupt();
      while (photothread.isAlive()) {}
      photothread = null;
    }
  }

  public void desktop()
    throws AWTException, IOException
  {
    stopPhotoThread();
    photothread = new PhotoThread(this);
    photothread.start();
  }

  protected static Service[] formatSearch(ServiceInfo[] paramArrayOfServiceInfo)
    throws IOException
  {
    Service[] arrayOfService = new Service[paramArrayOfServiceInfo.length];
    for (int i = 0; i < paramArrayOfServiceInfo.length; i++)
    {
      ServiceInfo localServiceInfo = paramArrayOfServiceInfo[i];
      Inet4Address[] arrayOfInet4Address = localServiceInfo.getInet4Addresses();
      arrayOfService[i] = new Service(arrayOfInet4Address[0].getHostAddress(), localServiceInfo.getPort(), localServiceInfo.getName());
    }
    return arrayOfService;
  }

  public static Service[] search()
    throws IOException
  {
    return search(6000);
  }

  public static Service[] search(int paramInt)
    throws IOException
  {
    JmDNS localJmDNS = JmDNS.create();
    Service[] arrayOfService = formatSearch(localJmDNS.list("_airplay._tcp.local.", paramInt));
    localJmDNS.close();
    return arrayOfService;
  }

  public static AirPlay searchDialog(Window paramWindow)
    throws IOException
  {
    return searchDialog(paramWindow, 6000);
  }

  public static AirPlay searchDialog(Window paramWindow, int paramInt)
    throws IOException
  {
    JDialog localJDialog = new JDialog(paramWindow, "Searching...");
    localJDialog.setVisible(true);
    localJDialog.setBounds(0, 0, 200, 100);
    localJDialog.setLocationRelativeTo(paramWindow);
    localJDialog.toFront();
    localJDialog.setVisible(true);
    Service[] arrayOfService = search();
    localJDialog.setVisible(false);
    if (arrayOfService.length > 0)
    {
      String[] arrayOfString = new String[arrayOfService.length];
      for (int i = 0; i < arrayOfService.length; i++) {
        arrayOfString[i] = (name + " (" + hostname + ")");
      }
      String str = (String)JOptionPane.showInputDialog(paramWindow, "", "Select AppleTV", -1, null, arrayOfString, arrayOfString[0]);
      if (str != null)
      {
        int j = -1;
        for (int k = 0; k < arrayOfString.length; k++) {
          if (str == arrayOfString[k])
          {
            j = k;
            break;
          }
        }
        AirPlay localAirPlay = new AirPlay(arrayOfService[j]);
        localAirPlay.setAuth(new AuthDialog(paramWindow));
        return localAirPlay;
      }
      return null;
    }
    throw new IOException("No AppleTVs Found");
  }

  public static void usage()
  {
    System.out.println("commands: -s {stop} | -p file {photo} | -d {desktop}");
    System.out.println("java -jar airplay.jar -h hostname[:port] [-a password] command");
  }

  public static String waitforuser()
  {
    return waitforuser("Press return to quit");
  }

  public static String waitforuser(String paramString)
  {
    System.out.println(paramString);
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
    String str = null;
    try
    {
      while (((str = localBufferedReader.readLine()) != null) && (str.length() < 0)) {}
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return str;
  }

  public static void main(String[] paramArrayOfString)
  {
    try
    {
      CmdLineParser localCmdLineParser = new CmdLineParser();
      CmdLineParser.Option localOption1 = localCmdLineParser.addStringOption('h', "hostname");
      CmdLineParser.Option localOption2 = localCmdLineParser.addBooleanOption('s', "stop");
      CmdLineParser.Option localOption3 = localCmdLineParser.addStringOption('p', "photo");
      CmdLineParser.Option localOption4 = localCmdLineParser.addBooleanOption('d', "desktop");
      CmdLineParser.Option localOption5 = localCmdLineParser.addStringOption('a', "password");
      localCmdLineParser.parse(paramArrayOfString);
      String str1 = (String)localCmdLineParser.getOptionValue(localOption1);
      if (str1 == null)
      {
        usage();
      }
      else
      {
        String[] arrayOfString = str1.split(":", 2);
        AirPlay localAirPlay;
        if (arrayOfString.length > 1) {
          localAirPlay = new AirPlay(arrayOfString[0], Integer.parseInt(arrayOfString[1]));
        } else {
          localAirPlay = new AirPlay(arrayOfString[0]);
        }
        localAirPlay.setAuth(new AuthConsole());
        String str2 = (String)localCmdLineParser.getOptionValue(localOption5);
        localAirPlay.setPassword(str2);
        if (localCmdLineParser.getOptionValue(localOption2) != null)
        {
          localAirPlay.stop();
        }
        else
        {
          String str3;
          if ((str3 = (String)localCmdLineParser.getOptionValue(localOption3)) != null)
          {
            System.out.println("Press ctrl-c to quit");
            localAirPlay.photo(str3);
          }
          else if (localCmdLineParser.getOptionValue(localOption4) != null)
          {
            System.out.println("Press ctrl-c to quit");
            localAirPlay.desktop();
          }
          else
          {
            usage();
          }
        }
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public static class Service
  {
    public String name;
    public String hostname;
    public int port;

    public Service(String paramString)
    {
      this(paramString, 7000);
    }

    public Service(String paramString, int paramInt)
    {
      this(paramString, paramInt, paramString);
    }

    public Service(String paramString1, int paramInt, String paramString2)
    {
      hostname = paramString1;
      port = paramInt;
      name = paramString2;
    }
  }

  public static class AuthConsole
    implements AirPlay.Auth
  {
    public AuthConsole() {}

    public String getPassword(String paramString1, String paramString2)
    {
      String str = paramString2 + " (" + paramString1 + ")";
      return AirPlay.waitforuser("Please input password for " + str);
    }
  }

  public static class AuthDialog
    implements AirPlay.Auth
  {
    private Window parent;

    public AuthDialog(Window paramWindow)
    {
      parent = paramWindow;
    }

    public String getPassword(String paramString1, String paramString2)
    {
      JPasswordField localJPasswordField = new JPasswordField();
      JOptionPane localJOptionPane = new JOptionPane(localJPasswordField, -1, 2);
      JDialog localJDialog = localJOptionPane.createDialog(parent, "Password:");
      localJDialog.setLocationRelativeTo(parent);
      localJDialog.setVisible(true);
      int i = ((Integer)localJOptionPane.getValue()).intValue();
      localJDialog.dispose();
      if (i == 0) {
        return new String(localJPasswordField.getPassword());
      }
      return null;
    }
  }

  public static abstract interface Auth
  {
    public abstract String getPassword(String paramString1, String paramString2);
  }

  public class PhotoThread
    extends Thread
  {
    private final AirPlay airplay;
    private BufferedImage image = null;
    private int timeout = 5000;

    public PhotoThread(AirPlay paramAirPlay)
    {
      this(paramAirPlay, null, 1000);
    }

    public PhotoThread(AirPlay paramAirPlay, BufferedImage paramBufferedImage, int paramInt)
    {
      airplay = paramAirPlay;
      image = paramBufferedImage;
      timeout = paramInt;
    }

    public void run()
    {
      for (;;)
      {
        if (!Thread.interrupted()) {
          try
          {
            if (image == null)
            {
              airplay.photoRaw(AirPlay.captureScreen(), "None");
            }
            else
            {
              airplay.photoRaw(image, "None");
              Thread.sleep(Math.round(0.9D * timeout));
            }
          }
          catch (InterruptedException localInterruptedException) {}catch (Exception localException)
          {
            localException.printStackTrace();
          }
        }
      }
    }
  }
}
